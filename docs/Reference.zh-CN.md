# React Native Simple OpenVPN API Reference

简体中文 | [English](./Reference.md)

本文档列出了 React Native Simple OpenVPN 当前提供的属性与方法

## 方法

| 名称                   | iOS | Android | 参数                                                     | 返回值  | 描述                      |
| ---------------------- | --- | ------- | -------------------------------------------------------- | ------- | ------------------------- |
| connect                | ✅  | ✅      | options: [VpnOptions](#vpnoptions)                       | promise | 连接 OpenVPN              |
| disconnect             | ✅  | ✅      | 无                                                       | promise | 关闭 OpenVPN 连接         |
| getCurrentState        | ✅  | ✅      | none                                                     | promise | 获取 VPN 当前状态         |
| observeState           | ✅  | ❌      | 无                                                       | promise | 监听 VPN 状态             |
| stopObserveState       | ✅  | ❌      | 无                                                       | promise | 停止监听 VPN 状态         |
| addVpnStateListener    | ✅  | ✅      | callback: (e: [VpnEventParams](#vpneventparams)) => void | void    | 添加 VPN 状态变更事件监听 |
| removeVpnStateListener | ✅  | ✅      | 无                                                       | void    | 移除 VPN 状态变更事件监听 |

## 属性

| 名称       | 值                                                                                                                                                        | 描述                               |
| ---------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------- |
| VpnState   | VPN_STATE_DISCONNECTED = 0 <br/> VPN_STATE_CONNECTING = 1 <br/> VPN_STATE_CONNECTED = 2 <br/> VPN_STATE_DISCONNECTING = 3 <br/> VPN_OTHER_STATE = 4 <br/> | VPN 当前状态                       |
| CompatMode | MODERN_DEFAULTS = 0 <br/> OVPN_TWO_FIVE_PEER = 1 <br/> OVPN_TWO_FOUR_PEER = 2 <br/> OVPN_TWO_THREE_PEER = 3 <br>                                          | OpenVPN 兼容模式(**Android only**) |

## 类型

### VpnOptions

```ts
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
  allowedAppsVpn?: Array<string>;
  allowedAppsVpnAreDisallowed?: boolean;
  providerBundleIdentifier: string;
  localizedDescription?: string;
}
```

#### remoteAddress

VPN 服务端地址，格式为 `<ip> <port>`，未传入时使用 `xxx.ovpn` 配置文件中的地址

#### ovpnString

OpenVPN 配置文件内容的字符串形式，未传入时或传递空字符串 `''`，模块将寻找 `xxx.ovpn` 配置文件

字符串格式参考 [example.ovpn](../example/android/app/src/main/assets/Japan.ovpn)

#### ovpnFileName

OpenVPN 配置文件的名称，不含扩展名，未传入时使用默认名称 `client`

#### username

鉴权用户名, 未传入时使用默认名称 `''`

#### password

鉴权密码, 未传入时使用默认名称 `''`

#### assetsPath

**Android 专用**，OpenVPN 配置文件在 `android/app/src/main/assets/` 下的路径

- 未传入时 `assetsPath` 为 `''`，文件路径为 `assets/xxx.ovpn`
- 传入路径时，比如 `'ovpn/'`，文件路径为 `assets/ovpn/xxx.ovpn`

#### notificationTitle

**Android 专用**，通知的标题，未传入时使用默认值 `OpenVPN`

#### compatMode

**Android 专用**，[OpenVPN 兼容模式](#属性)，未传入时使用默认值 `MODERN_DEFAULTS`

| 模式                | 描述                          |
| ------------------- | ----------------------------- |
| MODERN_DEFAULTS     | Modern defaults               |
| OVPN_TWO_FIVE_PEER  | OpenVPN 2.5.x peers           |
| OVPN_TWO_FOUR_PEER  | OpenVPN 2.4.x peers           |
| OVPN_TWO_THREE_PEER | OpenVPN 2.3.x and older peers |

#### useLegacyProvider

**Android 专用**，是否加载 OpenSSL legacy provider，未传入时使用默认值 `false`

#### useCustomConfig

**Android 专用**， 是否使用自定义配置，未传入时使用默认值 `false`

#### customConfigOptions

**Android 专用**，添加类似下方的配置字符串，未传入时使用默认值 `''`

```text
http-proxy ...
http-proxy-option ...
```

#### allowedAppsVpn

**Android 专用**, 进行 VPN 连接的应用包名列表, 默认值为空数组

```js
['com.app1', 'com.app2'];
```

#### allowedAppsVpnAreDisallowed

**Android 专用**, 控制 **allowedAppsVpn** 中所列的应用是否使用 VPN 连接, 而剩下的应用则相反, 未传入时使用默认值 `true`

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

如果你不使用 [ovpnString](#ovpnstring)，不要忘记将配置文件添加你的项目中

- Android 路径 `android/app/src/main/assets/`，没有 `assets` 文件夹就新建一个
- iOS 路径为 main bundle，将文件拖拽到工程下即可

  ![ios-ovpn-file](../.github/images/ios-ovpn-file.png)

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
