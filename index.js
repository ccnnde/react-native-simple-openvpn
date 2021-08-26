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
