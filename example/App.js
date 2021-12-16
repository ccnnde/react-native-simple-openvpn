import React, { useState, useEffect, useRef } from 'react';
import { SafeAreaView, StyleSheet, Platform, View, ScrollView, Text, Button } from 'react-native';
import RNSimpleOpenvpn, { addVpnStateListener, removeVpnStateListener } from 'react-native-simple-openvpn';

const isIPhone = Platform.OS === 'ios';
const PRIMARY_COLOR = 'skyblue';

const App = () => {
  const [log, setLog] = useState('');
  const logScrollView = useRef(null);

  useEffect(() => {
    async function observeVpn() {
      if (isIPhone) {
        await RNSimpleOpenvpn.observeState();
      }

      addVpnStateListener((e) => {
        updateLog(JSON.stringify(e), undefined, 2);
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
        remoteAddress: '',
        ovpnFileName: 'Japan', // Japan or Russian (android assets folder)
        assetsPath: '',
        providerBundleIdentifier: 'com.your.network.extension.bundle.id',
        localizedDescription: 'TestRNSimpleOvpn',
      });
    } catch (error) {
      updateLog(error);
    }
  }

  async function stopOvpn() {
    try {
      await RNSimpleOpenvpn.disconnect();
    } catch (error) {
      updateLog(error);
    }
  }

  function printVpnState() {
    updateLog(JSON.stringify(RNSimpleOpenvpn.VpnState, undefined, 2));
  }

  function updateLog(newLog) {
    const now = new Date().toLocaleTimeString();
    setLog(`${log}\n[${now}] ${newLog}`);
  }

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.btnContainer}>
        <Button title="Connect" color={PRIMARY_COLOR} onPress={startOvpn} />
        <Button title="Disconnect" color={PRIMARY_COLOR} onPress={stopOvpn} />
        <Button title="Vpn State" color={PRIMARY_COLOR} onPress={printVpnState} />
        <Button title="Clean Log" color={PRIMARY_COLOR} onPress={() => setLog('')} />
      </View>
      <View style={styles.logContainer}>
        <ScrollView
          ref={logScrollView}
          style={styles.logScroll}
          onContentSizeChange={() => logScrollView.current.scrollToEnd({ animted: true })}
        >
          <Text>{log}</Text>
        </ScrollView>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: '20%',
    alignItems: 'center',
  },
  btnContainer: {
    width: '80%',
    height: '25%',
    justifyContent: 'space-between',
  },
  logContainer: {
    width: '80%',
    height: '50%',
    borderColor: PRIMARY_COLOR,
    borderWidth: 2,
    marginTop: 10,
    padding: 10,
  },
  logScroll: {
    flex: 1,
  },
});

export default App;
