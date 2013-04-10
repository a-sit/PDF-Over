#!/bin/sh
BASEDIR=`dirname $0`
(cd $BASEDIR/.. && exec java -XstartOnFirstThread -cp "lib/*" at.asit.pdfover.gui.Main "$@")
