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

#import "RNSimpleOpenvpn.h"

NSString *const RN_OPEN_VPN = @"RNSimpleOpenvpn";
NSString *const STATE_CHANGED_EVENT = @"stateChanged";

typedef NS_ENUM(NSInteger, VpnState) {
  VpnStateDisconnected,
  VpnStateConnecting,
  VpnStateConnected,
  VpnStateDisconnecting,
  VpnOtherState,
};

@implementation RNSimpleOpenvpn

RCT_EXPORT_MODULE();

static NETunnelProviderManager *_cachedManager;

+ (void)dispose {
  if (_cachedManager) {
    [_cachedManager.connection stopVPNTunnel];
  }
}

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

- (NSDictionary *)constantsToExport {
  return @{
    @"VpnState" : @{
      @"VPN_STATE_DISCONNECTED" : @(VpnStateDisconnected),
      @"VPN_STATE_CONNECTING" : @(VpnStateConnecting),
      @"VPN_STATE_CONNECTED" : @(VpnStateConnected),
      @"VPN_STATE_DISCONNECTING" : @(VpnStateDisconnecting),
      @"VPN_OTHER_STATE" : @(VpnOtherState),
    }
  };
};

- (NSArray<NSString *> *)supportedEvents {
  return @[ STATE_CHANGED_EVENT ];
}

RCT_EXPORT_METHOD(connect
                  : (NSDictionary *)options resolver
                  : (RCTPromiseResolveBlock)resolve rejecter
                  : (RCTPromiseRejectBlock)reject) {
  self.ovpnOptions = options;
  [self prepareVpn:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(disconnect : (RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  [self.providerManager.connection stopVPNTunnel];
  _cachedManager = nil;
  resolve(nil);
}

- (void)prepareVpn:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
  [NETunnelProviderManager loadAllFromPreferencesWithCompletionHandler:^(
                               NSArray<NETunnelProviderManager *> *_Nullable managers, NSError *_Nullable error) {
    if (error) {
      reject(@"E_PREPARE_ERRROR", @"Prepare VPN failed", error);
      return;
    }

    self.providerManager = managers.firstObject ? managers.firstObject : [NETunnelProviderManager new];
    _cachedManager = self.providerManager;
    [self startVpn:resolve rejecter:reject];
  }];
}

- (void)startVpn:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
  NSString *const providerBundleIdentifier = self.ovpnOptions[@"providerBundleIdentifier"];
  NSString *const localizedDescription =
      self.ovpnOptions[@"localizedDescription"] ? self.ovpnOptions[@"localizedDescription"] : RN_OPEN_VPN;

  if (providerBundleIdentifier == nil) {
    reject(@"E_NO_BUNDLE_ERROR", @"There are no bundle identifier", nil);
    return;
  }

  NSString *const ovpnString = self.ovpnOptions[@"ovpnString"] ? self.ovpnOptions[@"ovpnString"] : @"";
  NSData *data;

  if ([ovpnString length] != 0) {
    data = [ovpnString dataUsingEncoding:NSUTF8StringEncoding];
  } else {
    NSString *const ovpnFileName = self.ovpnOptions[@"ovpnFileName"] ? self.ovpnOptions[@"ovpnFileName"] : @"client";
    NSURL *url = [[NSBundle mainBundle] URLForResource:ovpnFileName withExtension:@"ovpn"];

    if (url == nil) {
      reject(@"E_NO_OVPN_FILE_ERROR", @"There are no ovpn file", nil);
      return;
    }

    data = [[NSData alloc] initWithContentsOfURL:url];
  }

  NETunnelProviderProtocol *tunel = [NETunnelProviderProtocol new];
  tunel.providerConfiguration = @{@"ovpn" : data};
  tunel.providerBundleIdentifier = providerBundleIdentifier;
  tunel.serverAddress = @"";
  tunel.disconnectOnSleep = NO;

  self.providerManager.localizedDescription = localizedDescription;
  [self.providerManager setEnabled:YES];
  [self.providerManager setProtocolConfiguration:tunel];
  [self.providerManager saveToPreferencesWithCompletionHandler:^(NSError *error) {
    if (error) {
      reject(@"E_SAVE_PREFERENCE_ERROR", @"Provider Manager save preferences failed", error);
      return;
    }

    [self.providerManager loadFromPreferencesWithCompletionHandler:^(NSError *_Nullable error) {
      if (error) {
        reject(@"E_LOAD_PREFERENCE_ERROR", @"Provider Manager load preferences failed", error);
      } else {
        NSError *error = nil;
        NSString *const username = self.ovpnOptions[@"username"] ? self.ovpnOptions[@"username"] : @"";
        NSString *const password = self.ovpnOptions[@"password"] ? self.ovpnOptions[@"password"] : @"";
        NSString *const remoteAddress = self.ovpnOptions[@"remoteAddress"] ? self.ovpnOptions[@"remoteAddress"] : @"";

        [self.providerManager.connection
            startVPNTunnelWithOptions:@{@"username" : username, @"password" : password, @"remote" : remoteAddress}
                       andReturnError:&error];

        if (error) {
          reject(@"E_START_VPN_ERROR", @"Start VPN failed", error);
          return;
        }

        resolve(nil);
      }
    }];
  }];
}

RCT_EXPORT_METHOD(observeState : (RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  NSNotificationCenter *center = [NSNotificationCenter defaultCenter];

  self.vpnStateObserver = [center addObserverForName:NEVPNStatusDidChangeNotification
                                              object:nil
                                               queue:nil
                                          usingBlock:^(NSNotification *notification) {
                                            NEVPNConnection *nevpnConnection = (NEVPNConnection *)notification.object;
                                            [self sendEventWithName:STATE_CHANGED_EVENT
                                                               body:[self getVpnState:nevpnConnection.status]];
                                          }];

  resolve(nil);
}

RCT_EXPORT_METHOD(stopObserveState : (RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  NSNotificationCenter *center = [NSNotificationCenter defaultCenter];
  [center removeObserver:self.vpnStateObserver];
  resolve(nil);
}

RCT_EXPORT_METHOD(getCurrentState : (RCTPromiseResolveBlock)resolve rejecter : (RCTPromiseRejectBlock)reject) {
  NSDictionary *vpnState = [self getVpnState:self.providerManager.connection.status];
  resolve(vpnState[@"state"]);
}

- (NSDictionary *)getVpnState:(NEVPNStatus)status {
  VpnState state;
  NSString *message;

  switch (status) {
    case NEVPNStatusDisconnected:
      state = VpnStateDisconnected;
      message = @"The VPN is disconnected";
      break;
    case NEVPNStatusConnecting:
      state = VpnStateConnecting;
      message = @"The VPN is in the process of connecting";
      break;
    case NEVPNStatusConnected:
      state = VpnStateConnected;
      message = @"The VPN is connected";
      break;
    case NEVPNStatusDisconnecting:
      state = VpnStateDisconnecting;
      message = @"The VPN is in the process of disconnecting";
      break;
    case NEVPNStatusReasserting:
      state = VpnOtherState;
      message = @"The VPN is in the process of reconnecting";
      break;
    case NEVPNStatusInvalid:
      state = VpnOtherState;
      message = @"The VPN configuration does not exist in the Network Extension preferences or is not enabled";
      break;
    default:
      state = VpnOtherState;
      message = @"The VPN State is unknown";
  }

  return @{@"state" : @(state), @"message" : message};
}

@end
