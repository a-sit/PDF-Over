#!/bin/sh
BASEDIR=`dirname $0`
GDK_BACKEND=x11,wayland exec $BASEDIR/jre/bin/java -cp "$BASEDIR/lib/*" at.asit.pdfover.gui.Main "$@"
