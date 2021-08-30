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
