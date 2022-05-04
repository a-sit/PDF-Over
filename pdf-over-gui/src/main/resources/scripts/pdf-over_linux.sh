#!/bin/sh
BASEDIR=`dirname $0`
exec java -cp "$BASEDIR/lib/*" at.asit.pdfover.gui.Main "$@"
