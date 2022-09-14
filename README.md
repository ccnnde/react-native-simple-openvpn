# react-native-simple-openvpn [![github stars][github-star-img]][stargazers-url]

[![npm latest][version-img]][pkg-url]
[![download month][dl-month-img]][pkg-url]
[![download total][dl-total-img]][pkg-url]
![platforms][platform-img]
[![GNU General Public License][license-img]](LICENSE)

English | [简体中文](./README.zh-CN.md)

A simple react native module to interact with OpenVPN

If this project has helped you out, please support us with a star 🌟

## Versions

| RNSimpleOpenvpn | React Native |
| --------------- | ------------ |
| 1.0.0 ~ 1.2.0   | 0.56 ~ 0.66  |
| 2.0.0           | 0.63 ~ 0.68  |

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

### Android

Add the following to `android/settings.gradle` :

```diff
rootProject.name = 'example'
+ include ':vpnLib'
+ project(':vpnLib').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-simple-openvpn/vpnLib')
apply from: file("../node_modules/@react-native-community/cli-platform-android/native_modules.gradle"); applyNativeModulesSettingsGradle(settings)
include ':app'
```

#### Import jniLibs

Due to file size limitations, jniLibs are too big to be published on npm. Use the assets on [GitHub Releases](https://github.com/ccnnde/react-native-simple-openvpn/releases) instead

Download and unzip the resources you need for the corresponding architecture, and put them in `android/app/src/main/jniLibs` (create a new `jniLibs` folder if you don't have one)

```sh
project
├── android
│   ├── app
│   │   └── src
│   │       └── main
│   │           └── jniLibs
│   │               ├── arm64-v8a
│   │               ├── armeabi-v7a
│   │               ├── x86
│   │               └── x86_64
│   └── ...
├── ios
└── ...
```

### iOS

If using CocoaPods, run it in the `ios/` directory

```sh
pod install
```

See [iOS Guide](docs/iOS-Guide.md) for iOS side Network Extension configuration and OpenVPN integration

#### Disable VPN connection when app is terminated in iOS

Add the following to your project's `AppDelegate.m` :

```diff
+ #import "RNSimpleOpenvpn.h"

@implementation AppDelegate

// ...

+ - (void)applicationWillTerminate:(UIApplication *)application
+ {
+   [RNSimpleOpenvpn dispose];
+ }

@end
```

Please make sure the Header Search Paths of Build Settings contain the following paths:

```txt
$(SRCROOT)/../node_modules/react-native-simple-openvpn/ios
```

Or, if using CocoaPods, the following paths should be automatically included there:

```txt
"${PODS_ROOT}/Headers/Public/react-native-simple-openvpn"
```

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

| Name       | Value                                                                                                                                                     | Description                                  |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------- |
| VpnState   | VPN_STATE_DISCONNECTED = 0 <br/> VPN_STATE_CONNECTING = 1 <br/> VPN_STATE_CONNECTED = 2 <br/> VPN_STATE_DISCONNECTING = 3 <br/> VPN_OTHER_STATE = 4 <br/> | VPN Current Status                           |
| CompatMode | MODERN_DEFAULTS = 0 <br/> OVPN_TWO_FIVE_PEER = 1 <br/> OVPN_TWO_FOUR_PEER = 2 <br/> OVPN_TWO_THREE_PEER = 3 <br/>                                         | OpenVPN Compatibility Mode(**Android only**) |

## Types

### VpnOptions

```ts
interface VpnOptions {
  remoteAddress?: string;
  ovpnString?: string;
  ovpnFileName?: string;
  assetsPath?: string;
  notificationTitle?: string;
  compatMode?: RNSimpleOpenvpn.CompatMode;
  useLegacyProvider?: boolean;
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

#### notificationTitle

**Android only**，the title of the notification, using the default value `OpenVPN` if not passed in

#### compatMode

**Android only**，[OpenVPN compatibility mode](#properties), using the default value `MODERN_DEFAULTS` if not passed in

| Mode                | Description                   |
| ------------------- | ----------------------------- |
| MODERN_DEFAULTS     | Modern defaults               |
| OVPN_TWO_FIVE_PEER  | OpenVPN 2.5.x peers           |
| OVPN_TWO_FOUR_PEER  | OpenVPN 2.4.x peers           |
| OVPN_TWO_THREE_PEER | OpenVPN 2.3.x and older peers |

#### useLegacyProvider

**Android only**，load OpenSSL legacy provider or not, using the default value `false` if not passed in

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

- Android - [ics-openvpn](https://github.com/schwabe/ics-openvpn) v0.7.33
- iOS - [OpenVPNAdapter](https://github.com/ss-abramchuk/OpenVPNAdapter) v0.8.0

## Todo

- [x] Resolve RN 0.65 warning
- [x] Upgrade to the latest Android OpenVPN library

## License

[GPLv2](LICENSE) © Nor Cod

<!-- badge url -->

[pkg-url]: https://www.npmjs.com/package/react-native-simple-openvpn
[stargazers-url]: https://github.com/ccnnde/react-native-simple-openvpn/stargazers
[github-star-img]: https://img.shields.io/github/stars/ccnnde/react-native-simple-openvpn?label=Star%20Project&style=social
[version-img]: https://img.shields.io/npm/v/react-native-simple-openvpn?color=deepgreen&style=flat-square
[dl-month-img]: https://img.shields.io/npm/dm/react-native-simple-openvpn?style=flat-square
[dl-total-img]: https://img.shields.io/npm/dt/react-native-simple-openvpn?label=total&style=flat-square
[platform-img]: https://img.shields.io/badge/platforms-android%20|%20ios-lightgrey?style=flat-square
[license-img]: https://img.shields.io/badge/license-GPL%20v2-orange?style=flat-square
