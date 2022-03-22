#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")"; pwd)
export LC_CTYPE="UTF-8"
exec java -XstartOnFirstThread -cp "$BASEDIR/../lib/*" at.asit.pdfover.gui.Main "$@" &
