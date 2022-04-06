echo "<==> build ics-openvpn"
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-11.jdk/Contents/Home
cd ics-openvpn

echo "<==> init & update submodule"
git submodule update --init --force

echo "<==> clean proj"
./gradlew clean

echo "<==> build proj"
./gradlew build -Dorg.gradle.jvmargs=-Xmx2048M -PicsopenvpnDebugSign=true
