#!/bin/sh
BASEDIR=`dirname $0`
export GDK_BACKEND=X11
exec java -cp "$BASEDIR/../lib/*" at.asit.pdfover.gui.Main "$@"
