@import NetworkExtension;
@import OpenVPNAdapter;

@interface PacketTunnelProvider : NEPacketTunnelProvider <OpenVPNAdapterDelegate>

@property(nonatomic, strong) OpenVPNAdapter *vpnAdapter;

@property(nonatomic, strong) OpenVPNReachability *vpnReachability;

typedef void (^StartHandler)(NSError *_Nullable);
typedef void (^StopHandler)(void);

@property(nonatomic, copy) StartHandler startHandler;

@property(nonatomic, copy) StopHandler stopHandler;

@end
