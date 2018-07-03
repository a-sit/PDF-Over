#!/bin/sh
BASEDIR=`dirname $0`
java -jar "$BASEDIR/../lib/pdf-over-install-helper.jar"
exec java -cp "$BASEDIR/../lib/*" at.asit.pdfover.gui.Main "$@"
