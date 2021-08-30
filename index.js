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

import { NativeModules, NativeEventEmitter } from 'react-native';

const { RNSimpleOpenvpn } = NativeModules;
const localEventEmitter = new NativeEventEmitter(RNSimpleOpenvpn);
const stateListener = null;

export const addVpnStateListener = (callback) => {
  stateListener = localEventEmitter.addListener('stateChanged', (e) => callback(e));
};

export const removeVpnStateListener = () => {
  if (!stateListener) {
    return;
  }
  stateListener.remove();
  stateListener = null;
};

export default RNSimpleOpenvpn;
