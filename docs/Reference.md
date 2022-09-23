# React Native Simple OpenVPN API Reference

English | [简体中文](./Reference.zh-CN.md)

This document lays out the current public properties and methods for the React Native Simple OpenVPN

## Methods

| Name                   | iOS | Android | Parameters                                               | Return  | Description                                 |
| ---------------------- | --- | ------- | -------------------------------------------------------- | ------- | ------------------------------------------- |
| connect                | ✅  | ✅      | options: [VpnOptions](#vpnoptions)                       | promise | Connecting to OpenVPN                       |
| disconnect             | ✅  | ✅      | none                                                     | promise | Close the OpenVPN connection                |
| getCurrentState        | ✅  | ✅      | none                                                     | promise | Get current VPN status                      |
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

VPN server address, the format is `<ip> <port>`, use the address in the configuration file of `xxx.ovpn` when it is not passed in

#### ovpnString

The string format of the content of the OpenVPN configuration file. When it is not passed in or an empty string `''` is passed, the module will look for the `xxx.ovpn` configuration file string format

String format reference [example.ovpn](../example/android/app/src/main/assets/Japan.ovpn)

#### ovpnFileName

The name of the OpenVPN configuration file, without extensions, using the default name `client` if not passed in

#### username

The username for auth, using the default value `''` if not passed in

#### password

The password for auth, using the default value `''` if not passed in

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

#### useCustomConfig

**Android only**, use custom config or not, using the default value `false` if not passed in

#### customConfigOptions

**Android only**, add your own configuration string like below, using the default value `''` if not passed in

```text
http-proxy ...
http-proxy-option ...
```

#### allowedAppsVpn

**Android only**, list of application package names for VPN connection, the default value is an empty array

```js
['com.app1', 'com.app2'];
```

#### allowedAppsVpnAreDisallowed

**Android only**, the packages that we specify **allowedAppsVpn** use our VPN connection or not, and the rest is the opposite, using the default value `true` if not passed in

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

  ![ios-ovpn-file](../.github/images/ios-ovpn-file.png)

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
