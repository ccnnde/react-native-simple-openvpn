#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
@import NetworkExtension;

@interface RNSimpleOpenvpn : RCTEventEmitter <RCTBridgeModule>

@property(strong, nonatomic) NETunnelProviderManager *providerManager;

@property(strong, nonatomic) NSDictionary *ovpnOptions;

@property(strong, nonatomic) NSObject *vpnStateObserver;

@end