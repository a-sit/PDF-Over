#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")"; pwd)
GDK_BACKEND=x11,wayland exec "$BASEDIR/jre/bin/java" -cp "$BASEDIR/lib/*" at.asit.pdfover.gui.Main "$@"
