# react-native-simple-openvpn

[![npm version](https://img.shields.io/badge/npm-v1.0.0-brightgreen)](https://www.npmjs.com/package/react-native-simple-openvpn)
![platforms](https://img.shields.io/badge/platforms-android%20|%20ios-lightgrey)
[![GNU General Public License](https://img.shields.io/badge/license-GPL%20v2-orange)](http://www.gnu.org/licenses/gpl-2.0.html)

简体中文 | [English](./README.md)

A simple react native module to interact with OpenVPN

## 版本

| RNSimpleOpenvpn                                                     | React Native |
| ------------------------------------------------------------------- | ------------ |
| ![npm version](https://img.shields.io/badge/npm-v1.0.0-brightgreen) | 0.56 ~ 0.65  |

## 预览

<p>
  <img src="./.github/images/openvpn-android.gif" height="450" />
  <img src="./.github/images/openvpn-ios.gif" height="450" />
</p>

## 安装

### 添加依赖

```sh
# npm
npm install --save react-native-simple-openvpn

# or use yarn
yarn add react-native-simple-openvpn
```

### Link

从 react-native 0.60 开始，autolinking 将负责链接的步骤

```sh
react-native link react-native-simple-openvpn
```

### iOS

如果使用 CocoaPods, 在 `ios/` 目录下运行

```sh
pod install
```

iOS 端 Network Extension 配置以及 OpenVPN 的集成请参阅 [iOS 指南](docs/iOS-Guide.zh-CN.md)

## 用法

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

## 方法

| 名称                   | iOS | Android | 参数                                                     | 返回值  | 描述                      |
| ---------------------- | --- | ------- | -------------------------------------------------------- | ------- | ------------------------- |
| connect                | ✅  | ✅      | options: [VpnOptions](#vpnoptions)                       | promise | 连接 OpenVPN              |
| disconnect             | ✅  | ✅      | 无                                                       | promise | 关闭 OpenVPN 连接         |
| observeState           | ✅  | ❌      | 无                                                       | promise | 监听 VPN 状态             |
| stopObserveState       | ✅  | ❌      | 无                                                       | promise | 停止监听 VPN 状态         |
| addVpnStateListener    | ✅  | ✅      | callback: (e: [VpnEventParams](#vpneventparams)) => void | void    | 添加 VPN 状态变更事件监听 |
| removeVpnStateListener | ✅  | ✅      | 无                                                       | void    | 移除 VPN 状态变更事件监听 |

## 属性

| 名称     | 值                                                                                                                                                        | 描述         |
| -------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------ |
| VpnState | VPN_STATE_DISCONNECTED = 0 <br/> VPN_STATE_CONNECTING = 1 <br/> VPN_STATE_CONNECTED = 2 <br/> VPN_STATE_DISCONNECTING = 3 <br/> VPN_OTHER_STATE = 4 <br/> | VPN 当前状态 |

## 类型

### VpnOptions

```ts
interface VpnOptions {
  remoteAddress?: string;
  ovpnFileName?: string;
  assetsPath?: string;
  providerBundleIdentifier: string;
  localizedDescription?: string;
}
```

#### remoteAddress

VPN 服务端地址，格式为 `<ip> <port>`，未传入时使用 `xxx.ovpn` 配置文件中的地址

#### ovpnFileName

OpenVPN 配置文件的名称，不含扩展名，未传入时使用默认名称 `client`

#### assetsPath

**Android 专用**，OpenVPN 配置文件在 `android/app/src/main/assets/` 下的路径

- 未传入时 `assetsPath` 为 `''`，文件路径为 `assets/xxx.ovpn`
- 传入路径时，比如 `'ovpn/'`，文件路径为 `assets/ovpn/xxx.ovpn`

#### providerBundleIdentifier

**iOS 专用**，Network Extension target 的 bundle identifier

#### localizedDescription

**iOS 专用**，应用在设置 -> VPN 中的本地化描述名称，未传入时使用默认名称 `RNSimpleOpenvpn`

### VpnEventParams

```ts
interface VpnEventParams {
  state: RNSimpleOpenvpn.VpnState; // VPN 状态
  message: string; // VPN 状态相关的消息
  level?: string; // Android OpenVPN 库提供的连接状态描述
}
```

## 注意

### `xxx.ovpn` 配置文件

不要忘记将配置文件添加你的项目中

- Android 路径 `android/app/src/main/assets/`，没有 `assets` 文件夹就新建一个
- iOS 路径为 main bundle，将文件拖拽到工程下即可

  ![ios-ovpn-file](.github/images/ios-ovpn-file.png)

文件中 `remote` 所在行的格式必须如下形式

```text
...
remote <IP address> <port>
...
```

如果你不需要动态修改配置文件中的 `remote` 地址，则 Android 和 iOS 使用相同的配置文件，`options` 的 `remoteAddress` 可不传

但是，如果你需要动态修改配置文件中的 `remote` 地址，则 iOS 端配置文件需要注释掉 `remote` 所在行（如下），且始终传递 `options` 的 `remoteAddress`

```text
...
;remote <IP address> <port>>
...
```

## OpenVPN library

本项目使用到了以下项目

- Android - [ics-openvpn](https://github.com/schwabe/ics-openvpn)，由于个人项目的原因，Android 端目前使用的是其核心库的较旧版本
- iOS - [OpenVPNAdapter](https://github.com/ss-abramchuk/OpenVPNAdapter) v0.8.0

## License

react-native-simple-openvpn 是在 GPLv2 许可证下提供的。参见 [LICENSE](LICENSE) 文件以了解更多信息
