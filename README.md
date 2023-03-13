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

| RNSimpleOpenvpn | React Native  |
| --------------- | ------------- |
| `1.0.0 ~ 1.2.0` | `0.56 ~ 0.66` |
| `>= 2.0.0`      | `>= 0.63`     |

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

Due to file size limitations, jniLibs are too big to be published on npm. Use the assets on [GitHub Releases](https://github.com/ccnnde/react-native-simple-openvpn/releases/tag/v2.0.0) instead

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

For more, read the [API Reference](docs/Reference.md)

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
