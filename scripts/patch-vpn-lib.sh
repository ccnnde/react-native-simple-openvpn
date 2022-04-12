echo "<==> patch vpn-lib"

# 1. Fix error: android.app.RemoteServiceException: Bad notification for startForeground
# 2. Support notification to close vpn connection directly
# 3. Support click notification to jump to the MainActivity of the application
# 4. Support custom notification title
git apply patches/OpenVPNService.java.patch
