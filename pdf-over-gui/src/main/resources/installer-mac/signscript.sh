#!/bin/sh

set -e

APP="$1"

codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f -vvvv --timestamp -o runtime "${APP}/Contents/MacOS/PDF-Over"
codesign -s "Developer ID Application: SIT Zentrum fuer sichere Informationstechnologie-Austria (9CYHJNG644)" -f --timestamp -o runtime "${APP}"

echo "finished signing script!"
echo "${DMG}"
