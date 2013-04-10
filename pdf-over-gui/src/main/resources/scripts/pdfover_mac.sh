#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")"; pwd)
export LC_CTYPE="UTF-8"
(cd $BASEDIR/PDF-Over && exec java -XstartOnFirstThread -cp "lib/*" at.asit.pdfover.gui.Main "$@" &)
