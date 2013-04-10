#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")"; pwd)
export LC_CTYPE="UTF-8"
cd "$BASEDIR"
chmod a+x pdf-over_mac.sh
cd ..
osacompile -e "do shell script \"$BASEDIR/pdf-over_mac.sh\"" -x -o PDF-Over.app
cp -f icons/icon.icns PDF-Over.app/Contents/Resources/applet.icns
