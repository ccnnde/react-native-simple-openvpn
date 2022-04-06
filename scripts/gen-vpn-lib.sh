echo "<==> generate vpn-lib"

echo "<==> copy .so"
rm -rf vpnLib/src/main/jniLibs/*
cp -r ics-openvpn/main/build/intermediates/cmake/uiRelease/obj/. vpnLib/src/main/jniLibs
rm -rf vpnLib/src/main/jniLibs/**/pie_openvpn*

echo "<==> copy pie_openvpn"
rm -rf vpnLib/src/main/assets/*
cp -r ics-openvpn/main/build/ovpnassets/. vpnLib/src/main/assets

echo "<==> copy ovpn core"
rm -rf vpnLib/src/main/java/*
cp -r ics-openvpn/main/src/main/java/. vpnLib/src/main/java

rm -rf vpnLib/src/main/aidl/*
cp -r ics-openvpn/main/src/main/aidl/. vpnLib/src/main/aidl

rm -rf vpnLib/src/main/res/*
cp -r ics-openvpn/main/src/main/res/. vpnLib/src/main/res

rm -rf vpnLib/src/ui/java/**/*.{java,kt}
cp ics-openvpn/main/src/ui/java/de/blinkt/openvpn/core/VariantConfig.java vpnLib/src/ui/java/de/blinkt/openvpn/core
cp ics-openvpn/main/src/ui/java/de/blinkt/openvpn/fragments/Utils.kt vpnLib/src/ui/java/de/blinkt/openvpn/fragments
cp ics-openvpn/main/src/ui/java/de/blinkt/openvpn/activities/InternalWebView.kt vpnLib/src/ui/java/de/blinkt/openvpn/activities

rm -rf vpnLib/src/ui/res/drawable-*
rm -rf vpnLib/src/ui/res/values-*
cp -r ics-openvpn/main/src/ui/res/drawable-* vpnLib/src/ui/res
cp -r ics-openvpn/main/src/ui/res/values-* vpnLib/src/ui/res
cp ics-openvpn/main/src/ui/res/values/refs.xml vpnLib/src/ui/res/values
cp ics-openvpn/main/src/ui/res/layout/webview_internal.xml vpnLib/src/ui/res/layout
