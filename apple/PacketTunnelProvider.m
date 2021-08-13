#import "PacketTunnelProvider.h"
#import <NetworkExtension/NetworkExtension.h>

@interface NEPacketTunnelFlow () <OpenVPNAdapterPacketFlow>

@end

@implementation PacketTunnelProvider

- (OpenVPNAdapter *)vpnAdapter {
  if (!_vpnAdapter) {
    _vpnAdapter = [OpenVPNAdapter new];
    _vpnAdapter.delegate = self;
  }
  return _vpnAdapter;
}

- (OpenVPNReachability *)vpnReachability {
  if (!_vpnReachability) {
    _vpnReachability = [OpenVPNReachability new];
  }
  return _vpnReachability;
}

- (void)startTunnelWithOptions:(NSDictionary<NSString *, NSObject *> *)options
             completionHandler:(void (^)(NSError *_Nullable))completionHandler {
  NETunnelProviderProtocol *proto = (NETunnelProviderProtocol *)self.protocolConfiguration;

  if (!proto) {
    return;
  }

  NSDictionary<NSString *, id> *provider = proto.providerConfiguration;
  NSData *fileContent = provider[@"ovpn"];
  OpenVPNConfiguration *openVpnConfiguration = [OpenVPNConfiguration new];
  openVpnConfiguration.fileContent = fileContent;

  NSString *const remote = options[@"remote"];
  if ([remote length] != 0) {
    openVpnConfiguration.settings = @{@"remote" : remote};
  }

  NSError *error;
  OpenVPNConfigurationEvaluation *evaluation = [self.vpnAdapter applyConfiguration:openVpnConfiguration error:&error];
  if (error) {
    completionHandler(error);
    return;
  }

  if (!evaluation.autologin) {
    OpenVPNCredentials *credentials = [OpenVPNCredentials new];
    credentials.username = [NSString stringWithFormat:@"%@", [options objectForKey:@"username"]];
    credentials.password = [NSString stringWithFormat:@"%@", [options objectForKey:@"password"]];
    [self.vpnAdapter provideCredentials:credentials error:&error];
    if (error) {
      completionHandler(error);
      return;
    }
  }

  [self.vpnReachability startTrackingWithCallback:^(OpenVPNReachabilityStatus status) {
    if (status == OpenVPNReachabilityStatusReachableViaWiFi) {
      [self.vpnAdapter reconnectAfterTimeInterval:5];
    }
  }];

  self.startHandler = completionHandler;
  [self.vpnAdapter connectUsingPacketFlow:self.packetFlow];
}

- (void)stopTunnelWithReason:(NEProviderStopReason)reason completionHandler:(void (^)(void))completionHandler {
  self.stopHandler = completionHandler;

  if ([self.vpnReachability isTracking]) {
    [self.vpnReachability stopTracking];
  }

  [self.vpnAdapter disconnect];
}

- (void)openVPNAdapter:(OpenVPNAdapter *)openVPNAdapter
    configureTunnelWithNetworkSettings:(NEPacketTunnelNetworkSettings *)networkSettings
                     completionHandler:(void (^)(NSError *_Nullable))completionHandler {
  networkSettings.DNSSettings.matchDomains = @[ @"" ];
  [self setTunnelNetworkSettings:networkSettings completionHandler:completionHandler];
}

- (void)openVPNAdapter:(OpenVPNAdapter *)openVPNAdapter
           handleEvent:(OpenVPNAdapterEvent)event
               message:(nullable NSString *)message {
  switch (event) {
    case OpenVPNAdapterEventConnected:
      if (self.reasserting) {
        self.reasserting = false;
      }
      if (self.startHandler) {
        self.startHandler(nil);
      }
      self.startHandler = nil;
      break;
    case OpenVPNAdapterEventDisconnected:
      if (self.vpnReachability.isTracking) {
        [self.vpnReachability stopTracking];
      }
      if (self.stopHandler) {
        self.stopHandler();
      }
      self.stopHandler = nil;
      break;
    case OpenVPNAdapterEventReconnecting:
      self.reasserting = true;
      break;
    default:
      break;
  }
}

- (void)openVPNAdapter:(OpenVPNAdapter *)openVPNAdapter handleError:(NSError *)error {
  BOOL fatal = (BOOL)[error userInfo][OpenVPNAdapterErrorFatalKey];

  if (fatal) {
    if (self.vpnReachability.isTracking) {
      [self.vpnReachability stopTracking];
    }

    if (self.startHandler) {
      self.startHandler(error);
      self.startHandler = nil;
    } else {
      [self cancelTunnelWithError:error];
    }
  }
}

- (void)openVPNAdapter:(OpenVPNAdapter *)openVPNAdapter handleLogMessage:(NSString *)logMessage {
  // Handle log messages
  NSLog(@"PacketTunnelProvider: openVPNAdapter: logMSg: %@", logMessage);
}

- (void)handleAppMessage:(NSData *)messageData completionHandler:(void (^)(NSData *))completionHandler {
  // Add code here to handle the message.
}

- (void)sleepWithCompletionHandler:(void (^)(void))completionHandler {
  // Add code here to get ready to sleep.
  completionHandler();
}

- (void)wake {
  // Add code here to wake up.
}

@end
