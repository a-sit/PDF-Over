#!/bin/sh
SCRIPTDIR=$(cd "$(dirname "$0")"; pwd)
BASEDIR=$(cd "$(dirname "$0")/.."; pwd)
export LC_CTYPE="UTF-8"
cd "$SCRIPTDIR"
java -jar "$BASEDIR/lib/pdf-over-install-helper-1.0.0.jar"
chmod a+x pdf-over_mac.sh
cd "$BASEDIR"
if [ -d "$BASEDIR.app" ]; then
	osacompile -e "do shell script \"$SCRIPTDIR/pdf-over_mac.sh\"" -x -o PDF-Over.app
	cp -f icons/icon.icns PDF-Over.app/Contents/Resources/applet.icns
else
	osacompile -e "do shell script \"$BASEDIR.app/Contents/scripts/pdf-over_mac.sh\"" -x -o "$BASEDIR.app"
	cp -f icons/icon.icns "$BASEDIR.app"/Contents/Resources/applet.icns
	mv "$BASEDIR"/* "$BASEDIR.app"/Contents/
	rmdir "$BASEDIR"
	ln -s "$BASEDIR.app" "$BASEDIR"
fi
