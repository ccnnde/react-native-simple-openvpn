package com.norcod.rnovpn;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import de.blinkt.openvpn.VpnProfile;
import de.blinkt.openvpn.core.ConfigParser;
import de.blinkt.openvpn.core.OpenVPNThread;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import de.blinkt.openvpn.core.VpnStatus.ConnectionStatus;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RNSimpleOpenvpnModule extends ReactContextBaseJavaModule implements VpnStatus.StateListener {

  private String TAG = RNSimpleOpenvpnModule.class.getSimpleName();
  private HashMap<String, Object> ovpnOptions;
  private static final int START_VPN_PROFILE = 70;
  private OpenVPNThread vpnThread = new OpenVPNThread();
  private VpnProfile vpnProfile;

  private enum VpnState {
    VPN_STATE_DISCONNECTED,
    VPN_STATE_CONNECTING,
    VPN_STATE_CONNECTED,
    VPN_STATE_DISCONNECTING,
    VPN_OTHER_STATE,
  }

  private static ReactApplicationContext reactContext;

  public RNSimpleOpenvpnModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    VpnStatus.addStateListener(this);
  }

  @Override
  public String getName() {
    return "RNSimpleOpenvpn";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    final Map<String, Object> vpnState = new HashMap<>();

    for (VpnState state : VpnState.values()) {
      vpnState.put(state.toString(), state.ordinal());
    }

    constants.put("VpnState", vpnState);
    return constants;
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    if (!reactContext.hasActiveCatalystInstance()) {
      return;
    }

    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  @ReactMethod
  public void connect(ReadableMap options, Promise promise) {
    ovpnOptions = options.toHashMap();
    prepareVpn(promise);
  }

  @ReactMethod
  public void disconnect(Promise promise) {
    try {
      vpnThread.stop();
      promise.resolve(null);
    } catch (Exception e) {
      promise.reject("E_STOP_OVPN_ERROR", "Stop ovpn failed: " + e.toString());
    }
  }

  private void prepareVpn(final Promise promise) {
    Activity currentActivity = getCurrentActivity();

    if (currentActivity == null) {
      promise.reject("E_ACTIVITY_ERROR", "Activity doesn't exist");
      return;
    }

    Intent intent = VpnService.prepare(currentActivity);

    if (intent != null) {
      reactContext.addActivityEventListener(new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
          if (resultCode == RESULT_OK && requestCode == START_VPN_PROFILE) {
            startVpn(promise);
          } else {
            promise.reject("E_PREPARE_ERRROR", "Prepare VPN failed");
          }
        }
      });

      currentActivity.startActivityForResult(intent, START_VPN_PROFILE);
      return;
    }

    startVpn(promise);
  }

  private void startVpn(Promise promise) {
    String config = "";

    try {
      String remoteAddress = ovpnOptions.getOrDefault("remoteAddress", "").toString();
      String assetsPath = ovpnOptions.getOrDefault("assetsPath", "").toString();
      String ovpnFileName = ovpnOptions.getOrDefault("ovpnFileName", "client").toString();
      String ovpnFilePath = assetsPath + ovpnFileName + ".ovpn";

      InputStream conf = reactContext.getAssets().open(ovpnFilePath);
      InputStreamReader isr = new InputStreamReader(conf);
      BufferedReader br = new BufferedReader(isr);
      String line;

      while (true) {
        line = br.readLine();
        if (line == null) {
          break;
        }
        config += line + "\n";
      }

      if (config.isEmpty()) {
        throw new Exception("ovpn config is empty");
      }

      if (!remoteAddress.isEmpty()) {
        config = getModifiedOvpnConfig(config, remoteAddress);
      }
    } catch (Exception e) {
      promise.reject("E_READ_OVPN_CONFIG_ERROR", "Read ovpn config failed: " + e.toString());
      return;
    }

    ConfigParser cp = new ConfigParser();

    try {
      // String username = ovpnOptions.getOrDefault("username", "").toString();
      // String password = ovpnOptions.getOrDefault("password", "").toString();

      cp.parseConfig(new StringReader(config));
      vpnProfile = cp.convertProfile();
      // vpnProfile.mUsername = username;
      // vpnProfile.mPassword = password;

      ProfileManager.setTemporaryProfile(vpnProfile);
      VPNLaunchHelper.startOpenVpn(vpnProfile, reactContext);
      promise.resolve(null);
    } catch (Exception e) {
      promise.reject("E_LOAD_OVPN_PROFILE_ERROR", "Load ovpn profile failed: " + e.toString());
    }
  }

  private String getModifiedOvpnConfig(String ovpnConfig, String remoteAddress) {
    final String regex =
        "^remote\\s(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\s((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4}))$";
    final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    final Matcher matcher = pattern.matcher(ovpnConfig);

    return matcher.replaceAll("remote " + remoteAddress);
  }

  @Override
  public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level) {
    WritableMap params = Arguments.createMap();
    params.putInt("state", getVpnState(level));
    params.putString("message", state);
    params.putString("level", level.toString());
    sendEvent("stateChanged", params);
  }

  private int getVpnState(ConnectionStatus level) {
    VpnState state;

    switch (level) {
      case LEVEL_NOTCONNECTED:
        state = VpnState.VPN_STATE_DISCONNECTED;
        break;
      case LEVEL_START:
      case LEVEL_WAITING_FOR_USER_INPUT:
      case LEVEL_CONNECTING_NO_SERVER_REPLY_YET:
      case LEVEL_CONNECTING_SERVER_REPLIED:
        state = VpnState.VPN_STATE_CONNECTING;
        break;
      case LEVEL_CONNECTED:
        state = VpnState.VPN_STATE_CONNECTED;
        break;
      default:
        state = VpnState.VPN_OTHER_STATE;
        break;
    }

    return state.ordinal();
  }
}
