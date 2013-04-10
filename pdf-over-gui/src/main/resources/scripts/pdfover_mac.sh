#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")"; pwd)
(cd $BASEDIR/PDFOver/Contents && exec java -XstartOnFirstThread -cp "lib/*" at.asit.pdfover.gui.Main "$@")
