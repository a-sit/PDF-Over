#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")"; pwd)
export LC_CTYPE="UTF-8"
#java -jar "$BASEDIR/../lib/pdf-over-install-helper-1.0.0.jar"
exec java -XstartOnFirstThread -cp "$BASEDIR/../lib/*" at.asit.pdfover.gui.Main "$@" &
