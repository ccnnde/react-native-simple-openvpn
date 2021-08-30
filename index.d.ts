interface VpnOptions {
  remoteAddress?: string;
  ovpnFileName?: string;
  assetsPath?: string;
  providerBundleIdentifier: string;
  localizedDescription?: string;
}

interface VpnEventParams {
  state: RNSimpleOpenvpn.VpnState;
  message: string;
  level?: string;
}

declare namespace RNSimpleOpenvpn {
  function connect(options: VpnOptions): Promise<void>;
  function disconnect(): Promise<void>;
  function observeState(): Promise<void>;
  function stopObserveState(): Promise<void>;
  enum VpnState {
    VPN_STATE_DISCONNECTED,
    VPN_STATE_CONNECTING,
    VPN_STATE_CONNECTED,
    VPN_STATE_DISCONNECTING,
    VPN_OTHER_STATE,
  }
}

declare const addVpnStateListener: (callback: (e: VpnEventParams) => void) => void;

declare const removeVpnStateListener: () => void;

export { addVpnStateListener, removeVpnStateListener, VpnOptions, VpnEventParams };

export default RNSimpleOpenvpn;
