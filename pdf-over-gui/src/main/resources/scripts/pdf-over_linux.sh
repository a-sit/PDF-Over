#!/bin/sh
BASEDIR=`dirname $0`
exec $BASEDIR/jre/bin/java -cp "$BASEDIR/lib/*" at.asit.pdfover.gui.Main "$@"
