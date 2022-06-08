/**
 * Copyright (C) 2021 Nor Cod
 *
 * This file is part of react-native-simple-openvpn.
 *
 * react-native-simple-openvpn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * react-native-simple-openvpn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with react-native-simple-openvpn.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.norcod.rnovpn;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ActivityEventListener;
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
import de.blinkt.openvpn.core.ConnectionStatus;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VPNLaunchHelper;
import de.blinkt.openvpn.core.VpnStatus;
import de.blinkt.openvpn.fragments.Utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.N)
public class RNSimpleOpenvpnModule extends ReactContextBaseJavaModule implements VpnStatus.StateListener {

  private String TAG = RNSimpleOpenvpnModule.class.getSimpleName();
  private HashMap<String, Object> ovpnOptions;
  private static final int START_VPN_PROFILE = 70;
  private VpnProfile vpnProfile;
  private Promise vpnPromise;

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      if (requestCode == START_VPN_PROFILE) {
        if (vpnPromise != null) {
          if (resultCode == RESULT_OK) {
            startVpn(vpnPromise);
          } else {
            vpnPromise.reject("E_PREPARE_ERROR", "Prepare VPN failed");
            vpnPromise = null;
          }
        }
      }
    }
  };

  private enum VpnState {
    VPN_STATE_DISCONNECTED,
    VPN_STATE_CONNECTING,
    VPN_STATE_CONNECTED,
    VPN_STATE_DISCONNECTING,
    VPN_OTHER_STATE,
  }

  private enum CompatMode {
    MODERN_DEFAULTS,
    OVPN_TWO_FIVE_PEER,
    OVPN_TWO_FOUR_PEER,
    OVPN_TWO_THREE_PEER,
  }

  private static ReactApplicationContext reactContext;

  private IOpenVPNServiceInternal mService;

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      mService = (IOpenVPNServiceInternal)(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mService = null;
    }
  };

  public RNSimpleOpenvpnModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    reactContext.addActivityEventListener(mActivityEventListener);
    VpnStatus.addStateListener(this);

    Intent intent = new Intent(context, OpenVPNService.class);
    intent.setAction(OpenVPNService.START_SERVICE);
    context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override
  public String getName() {
    return "RNSimpleOpenvpn";
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    final Map<String, Object> vpnState = new HashMap<>();
    final Map<String, Object> compatMode = new HashMap<>();

    for (VpnState state : VpnState.values()) {
      vpnState.put(state.toString(), state.ordinal());
    }

    for (CompatMode mode : CompatMode.values()) {
      compatMode.put(mode.toString(), mode.ordinal());
    }

    constants.put("VpnState", vpnState);
    constants.put("CompatMode", compatMode);

    return constants;
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    if (!reactContext.hasActiveCatalystInstance()) {
      return;
    }

    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
  }

  @ReactMethod
  public void addListener(String eventName) {
    // Set up any upstream listeners or background tasks as necessary
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    // Remove upstream listeners, stop unnecessary background tasks
  }

  @ReactMethod
  public void connect(ReadableMap options, Promise promise) {
    ovpnOptions = options.toHashMap();
    prepareVpn(promise);
  }

  @ReactMethod
  public void disconnect(Promise promise) {
    try {
      if (mService != null) {
        mService.stopVPN(false);
      }

      promise.resolve(null);
    } catch (Exception e) {
      promise.reject("E_STOP_OVPN_ERROR", "Stop ovpn failed: " + e.toString());
    }
  }

  @ReactMethod
  public void getCurrentState(Promise promise) {
    promise.resolve(getVpnState(VpnStatus.getStatus()));
  }

  private void prepareVpn(final Promise promise) {
    Activity currentActivity = getCurrentActivity();

    if (currentActivity == null) {
      promise.reject("E_ACTIVITY_ERROR", "Activity doesn't exist");
      return;
    }

    vpnPromise = promise;
    Intent intent = VpnService.prepare(currentActivity);

    if (intent != null) {
      currentActivity.startActivityForResult(intent, START_VPN_PROFILE);
      return;
    }

    startVpn(vpnPromise);
  }

  private void startVpn(Promise promise) {
    String config = "";

    try {
      String remoteAddress = ovpnOptions.getOrDefault("remoteAddress", "").toString();
      String ovpnString = ovpnOptions.getOrDefault("ovpnString", "").toString();

      config = ovpnString.isEmpty() ? getOvpnFileContent() : ovpnString;

      if (config.isEmpty()) {
        throw new Exception("ovpn config is empty");
      }

      if (!remoteAddress.isEmpty()) {
        config = getModifiedOvpnConfig(config, remoteAddress);
      }
    } catch (Exception e) {
      promise.reject("E_READ_OVPN_CONFIG_ERROR", "Read ovpn config failed: " + e.toString());
      promise = null;
      return;
    }

    ConfigParser cp = new ConfigParser();

    try {
      String username = ovpnOptions.getOrDefault("username", "").toString();
      String password = ovpnOptions.getOrDefault("password", "").toString();
      String notificationTitle = ovpnOptions.getOrDefault("notificationTitle", "OpenVPN").toString();
      int compatMode = ovpnOptions.get("compatMode") != null ? ((Double)ovpnOptions.get("compatMode")).intValue()
                                                             : CompatMode.MODERN_DEFAULTS.ordinal();
      boolean useLegacyProvider = (boolean)ovpnOptions.getOrDefault("useLegacyProvider", false);
      boolean useCustomConfig = (boolean)ovpnOptions.getOrDefault("useCustomConfig", false);
      String customConfigOptions = ovpnOptions.getOrDefault("customConfigOptions", "").toString();

      cp.parseConfig(new StringReader(config));
      vpnProfile = cp.convertProfile();
      vpnProfile.mUsername = username;
      vpnProfile.mPassword = password;
      vpnProfile.mName = notificationTitle;
      vpnProfile.mCompatMode = Utils.mapCompatMode(compatMode);
      vpnProfile.mUseLegacyProvider = useLegacyProvider;
      vpnProfile.mUseCustomConfig = useCustomConfig;
      vpnProfile.mCustomConfigOptions = customConfigOptions;

      if (vpnProfile.checkProfile(reactContext) != R.string.no_error_found) {
        throw new RemoteException(reactContext.getString(vpnProfile.checkProfile(reactContext)));
      }

      ProfileManager.setTemporaryProfile(reactContext, vpnProfile);
      VPNLaunchHelper.startOpenVpn(vpnProfile, reactContext);
      promise.resolve(null);
    } catch (Exception e) {
      promise.reject("E_LOAD_OVPN_PROFILE_ERROR", "Load ovpn profile failed: " + e.toString());
    }

    promise = null;
  }

  private String getOvpnFileContent() throws Exception {
    String content = "";
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
      content += line + "\n";
    }

    return content;
  }

  private String getModifiedOvpnConfig(String ovpnConfig, String remoteAddress) {
    final String regex =
        "^remote\\s(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\s((6553[0-5])|(655[0-2][0-9])|(65[0-4][0-9]{2})|(6[0-4][0-9]{3})|([1-5][0-9]{4})|([0-5]{0,5})|([0-9]{1,4}))$";
    final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    final Matcher matcher = pattern.matcher(ovpnConfig);

    return matcher.replaceAll("remote " + remoteAddress);
  }

  @Override
  public void updateState(String state, String logmessage, int localizedResId, ConnectionStatus level, Intent Intent) {
    WritableMap params = Arguments.createMap();
    params.putInt("state", getVpnState(level));
    params.putString("message", state);
    params.putString("level", level.toString());
    sendEvent("stateChanged", params);
  }

  public void setConnectedVPN(String uuid) {}

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
