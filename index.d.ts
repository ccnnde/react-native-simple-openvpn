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

interface VpnOptions {
  remoteAddress?: string;
  ovpnString?: string;
  ovpnFileName?: string;
  username?: string;
  password?: string;
  assetsPath?: string;
  notificationTitle?: string;
  compatMode?: RNSimpleOpenvpn.CompatMode;
  useLegacyProvider?: boolean;
  useCustomConfig?: boolean;
  customConfigOptions?: string;
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
  function getCurrentState(): Promise<VpnState>;
  function observeState(): Promise<void>;
  function stopObserveState(): Promise<void>;
  enum VpnState {
    VPN_STATE_DISCONNECTED,
    VPN_STATE_CONNECTING,
    VPN_STATE_CONNECTED,
    VPN_STATE_DISCONNECTING,
    VPN_OTHER_STATE,
  }
  enum CompatMode {
    MODERN_DEFAULTS,
    OVPN_TWO_FIVE_PEER,
    OVPN_TWO_FOUR_PEER,
    OVPN_TWO_THREE_PEER,
  }
}

declare const addVpnStateListener: (callback: (e: VpnEventParams) => void) => void;

declare const removeVpnStateListener: () => void;

export { addVpnStateListener, removeVpnStateListener, VpnOptions, VpnEventParams };

export default RNSimpleOpenvpn;
