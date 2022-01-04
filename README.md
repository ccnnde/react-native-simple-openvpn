# react-native-simple-openvpn

[![npm latest][version-img]][pkg-url]
[![download][download-img]][pkg-url]
![platforms][platform-img]
[![GNU General Public License][license-img]](LICENSE)

English | [简体中文](./README.zh-CN.md)

A simple react native module to interact with OpenVPN

## Versions

| RNSimpleOpenvpn                                                                      | React Native |
| ------------------------------------------------------------------------------------ | ------------ |
| ![npm v1.0.0](https://img.shields.io/badge/npm-v1.0.0-brightgreen?style=flat-square) | 0.56 ~ 0.66  |

## Preview

<p>
  <img src="./.github/images/openvpn-android.gif" height="450" alt="openvpn-android" />
  <img src="./.github/images/openvpn-ios.gif" height="450" alt="openvpn-ios" />
</p>

## Installation

### Adding dependencies

```sh
# npm
npm install --save react-native-simple-openvpn

# or use yarn
yarn add react-native-simple-openvpn
```

### Link

From react-native 0.60 autolinking will take care of the link step

```sh
react-native link react-native-simple-openvpn
```

### iOS

If using CocoaPods, run it in the `ios/` directory

```sh
pod install
```

See [iOS Guide](docs/iOS-Guide.md) for iOS side Network Extension configuration and OpenVPN integration

## Example

[Example](./example/README.md)

## Usage

```jsx
import React, { useEffect } from 'react';
import { Platform } from 'react-native';
import RNSimpleOpenvpn, { addVpnStateListener, removeVpnStateListener } from 'react-native-simple-openvpn';

const isIPhone = Platform.OS === 'ios';

const App = () => {
  useEffect(() => {
    async function observeVpn() {
      if (isIPhone) {
        await RNSimpleOpenvpn.observeState();
      }

      addVpnStateListener((e) => {
        // ...
      });
    }

    observeVpn();

    return async () => {
      if (isIPhone) {
        await RNSimpleOpenvpn.stopObserveState();
      }

      removeVpnStateListener();
    };
  });

  async function startOvpn() {
    try {
      await RNSimpleOpenvpn.connect({
        remoteAddress: '192.168.1.1 3000',
        ovpnFileName: 'client',
        assetsPath: 'ovpn/',
        providerBundleIdentifier: 'com.example.RNSimpleOvpnTest.NEOpenVPN',
        localizedDescription: 'RNSimpleOvpn',
      });
    } catch (error) {
      // ...
    }
  }

  async function stopOvpn() {
    try {
      await RNSimpleOpenvpn.disconnect();
    } catch (error) {
      // ...
    }
  }

  function printVpnState() {
    console.log(JSON.stringify(RNSimpleOpenvpn.VpnState, undefined, 2));
  }

  // ...
};

export default App;
```

## Methods

| Name                   | iOS | Android | Parameters                                               | Return  | Description                                 |
| ---------------------- | --- | ------- | -------------------------------------------------------- | ------- | ------------------------------------------- |
| connect                | ✅  | ✅      | options: [VpnOptions](#vpnoptions)                       | promise | Connecting to OpenVPN                       |
| disconnect             | ✅  | ✅      | none                                                     | promise | Close the OpenVPN connection                |
| observeState           | ✅  | ❌      | none                                                     | promise | Listening for VPN status                    |
| stopObserveState       | ✅  | ❌      | none                                                     | promise | Stop listening to VPN status                |
| addVpnStateListener    | ✅  | ✅      | callback: (e: [VpnEventParams](#vpneventparams)) => void | void    | Add VPN status change event listener        |
| removeVpnStateListener | ✅  | ✅      | none                                                     | void    | Remove the VPN status change event listener |

## Properties

| Name     | Value                                                                                                                                                     | Description        |
| -------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------ |
| VpnState | VPN_STATE_DISCONNECTED = 0 <br/> VPN_STATE_CONNECTING = 1 <br/> VPN_STATE_CONNECTED = 2 <br/> VPN_STATE_DISCONNECTING = 3 <br/> VPN_OTHER_STATE = 4 <br/> | VPN Current Status |

## Types

### VpnOptions

```ts
interface VpnOptions {
  remoteAddress?: string;
  ovpnString?: string;
  ovpnFileName?: string;
  assetsPath?: string;
  providerBundleIdentifier: string;
  localizedDescription?: string;
}
```

#### remoteAddress

VPN server address, the format is `<ip> <port>`, use the address in the configuration file of `xxx.ovpn` when it is not passed in

#### ovpnString

The string format of the content of the OpenVPN configuration file. When it is not passed in or an empty string `''` is passed, the module will look for the `xxx.ovpn` configuration file string format

String format reference [example.ovpn](example/android/app/src/main/assets/Japan.ovpn)

#### ovpnFileName

The name of the OpenVPN configuration file, without extensions, using the default name `client` if not passed in

#### assetsPath

**Android only**，the path to the OpenVPN configuration file under `android/app/src/main/assets/`

- `assetsPath` is `''` when not passed in, the file path is `assets/xxx.ovpn`
- When passing in a path, such as `'ovpn/'`, the file path is `assets/ovpn/xxx.ovpn`

#### providerBundleIdentifier

**iOS only**，the bundle identifier of the Network Extension target

#### localizedDescription

**iOS only**，the localized description name of the app in Settings -> VPN. If it is not passed in, the default name `RNSimpleOpenvpn` will be used

### VpnEventParams

```ts
interface VpnEventParams {
  state: RNSimpleOpenvpn.VpnState; // VPN Status
  message: string; // VPN Status Related Messages
  level?: string; // Description of the connection status provided by the Android OpenVPN library
}
```

## Attention

### `xxx.ovpn` configuration file

If you don’t use [ovpnString](#ovpnstring), don’t forget to add the configuration file to your project

- The Android path is `android/app/src/main/assets/`，create a new `assets` folder if you don't have one
- The iOS path is the main bundle, just drag and drop the file into the project

  ![ios-ovpn-file](.github/images/ios-ovpn-file.png)

The format of the line where `remote` is located in the file must be of the following form

```text
...
remote <IP address> <port>
...
```

If you don't need to dynamically modify the `remote` address in the configuration file, the same configuration file is used for Android and iOS, and the `remoteAddress` of `options` can be left out

However, if you need to dynamically change the `remote` address in the configuration file, the iOS configuration file needs to comment out the line where `remote` is located (below) and always pass `remoteAddress` for `options`

```text
...
;remote <IP address> <port>>
...
```

## OpenVPN library

The following items were used in this project

- Android - [ics-openvpn](https://github.com/schwabe/ics-openvpn), for personal project reasons, the Android side is currently using an older version of its core library
- iOS - [OpenVPNAdapter](https://github.com/ss-abramchuk/OpenVPNAdapter) v0.8.0

## Todo

- [ ] Resolve RN 0.65 warning
- [ ] Upgrade to the latest Android OpenVPN library

## License

[GPLv2](LICENSE) © Nor Cod

<!-- badge url -->

[pkg-url]: https://www.npmjs.com/package/react-native-simple-openvpn
[version-img]: https://img.shields.io/npm/v/react-native-simple-openvpn?color=deepgreen&style=flat-square
[download-img]: https://img.shields.io/npm/dm/react-native-simple-openvpn?style=flat-square
[platform-img]: https://img.shields.io/badge/platforms-android%20|%20ios-lightgrey?style=flat-square
[license-img]: https://img.shields.io/badge/license-GPL%20v2-orange?style=flat-square
