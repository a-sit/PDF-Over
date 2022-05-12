#!/bin/sh

# from: https://developer.apple.com/forums/thread/130855
# Fail if any command fails.

set -e

# Check and unpack the arguments.

if [ $# -ne 1 ]
then
    echo "usage: package-archive.sh /path/to.xcarchive" > /dev/stderr
    exit 1
fi
ARCHIVE="$1"

# Establish a work directory, create a disk image root directory within
# that, and then copy the app there.
#
# Note we use `-R`, not `-r`, to preserve symlinks.

WORKDIR="pdf-over-`date '+%Y-%m-%d_%H.%M.%S'`"
DMGROOT="${WORKDIR}/PDF-Over"
APP="${WORKDIR}/PDF-Over/PDF-Over.app"
DMG="${WORKDIR}/pdf-over.dmg"

mkdir -p "${DMGROOT}"
cp -R "${ARCHIVE}/PDF-Over.app" "${DMGROOT}/"

# When you use `-f` to replace a signature, `codesign` prints `replacing
# existing signature`.  There's no option to suppress that.  The message
# goes to `stderr` so you don't want to redirect it to `/dev/null` because
# there might be other interesting stuff logged to `stderr`.  One way to
# prevent it is to remove the signature beforehand, as shown by the
# following lines.  It does slow things down a bunch though, so I've made
# it easy to disable them.

if true
then
    codesign --remove-signature "${APP}/Contents/MacOS/PDF-Over"
    codesign --remove-signature "${APP}"
fi



codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f -vvvv  --timestamp -o runtime "${APP}/Contents/MacOS/PDF-Over"
#codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f --timestamp -o runtime "${APP}/Contents/Resources/bin/mocca.jar/BKULocal.war/WEB-INF/lib/smcc-1.4.2.jar/at/gv/egiz/smcc/osx-pcsc-jni/jre6.libosxj2pcsc.dylib"
#codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f --timestamp -o runtime "${APP}/Contents/Resources/bin/mocca.jar/BKULocal.war/WEB-INF/lib/smcc-1.4.2.jar/at/gv/egiz/smcc/osx-pcsc-jni/jre8.libosxj2pcsc.dylib"
#codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f --timestamp -o runtime "${APP}/Contents/Resources/bin/mocca.jar/BKULocal.war/WEB-INF/lib/smcc-1.4.2.jar/at/gv/egiz/smcc/osx-pcsc-jni/jre7.libosxj2pcsc.dylib"
codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f --timestamp -o runtime "${APP}"


# Create a disk image from our disk image root directory.

hdiutil create -srcFolder "${DMGROOT}" -quiet -o "${DMG}"

# Sign that.

codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -vvvv --timestamp -i at.egiz.PDF-Over  "${DMG}"

echo "finished signing script!"
echo "${DMG}"
