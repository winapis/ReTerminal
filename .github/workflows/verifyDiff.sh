set -e
BUILT_APK=$1

if [[ -z "$BUILT_APK" ]]; then
    echo "Error: No APK file provided as parameter"
    echo "Usage: $0 <path_to_apk_file>"
    exit 1
fi

if [[ ! -f "$BUILT_APK" ]]; then
    echo "Error: APK file not found: $BUILT_APK"
    exit 1
fi

echo "Comparing APK: $BUILT_APK"
curl -LO https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool
chmod +x apktool
export PATH=$PATH:$PWD
curl -LO https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.11.0.jar
mv apktool_2.11.0.jar apktool.jar
curl -LO https://raw.githubusercontent.com/daniellockyer/apkdiff/refs/heads/master/apkdiff.py

REPO="RohitKushvaha01/ReTerminal"
RELEASE_FILE="release.apk"

LATEST_RELEASE_URL="https://github.com/$REPO/releases/latest"
APK_URL=$(curl -s "$LATEST_RELEASE_URL" | grep -oP 'href="\K(/[^"]+\.apk)' | head -n 1)

if [[ -z "$APK_URL" ]]; then
    echo "No APK found!"
    exit 1
fi

APK_URL="https://github.com$APK_URL"
echo "Downloading APK from: $APK_URL"
curl -L -o "$RELEASE_FILE" "$APK_URL"

python apkdiff.py $BUILT_APK $RELEASE_FILE


