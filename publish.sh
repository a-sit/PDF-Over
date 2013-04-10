#!/bin/bash

#### CONFIGURE: ######
PUBLISH_DIR="pub"
LOG_DIR="log"
VERSION="4.0.0-SNAPSHOT"
CODEBASE_URL="http:\/\/10.27.152.123\/pdfover\/"
HOMEPAGE_URL="http:\/\/www.buergerkarte.at"


#### DON'T CONFIGURE ####

TARGET_FILE="pdf-over-gui-$VERSION-standard.jar"

echo "Publishing to: $PUBLISH_DIR"

mkdir -p $PUBLISH_DIR
mkdir -p $LOG_DIR

profiles=( linux linux-64 windows windows-64 mac mac-64 )
names=( linux_x86 linux_x64 windows_x86 windows_x64 mac_x86 mac_x64 )

for (( i = 0 ; i < ${#names[@]} ; i++ )) do
 	PROFILE=${profiles[$i]}
 	NAME=${names[$i]}
 	INSTALLER=setup_pdfover_$NAME.jar
 	echo -n "Building package [$PROFILE] as $INSTALLER ... "
 	mvn install -P$PROFILE > $LOG_DIR/build_$NAME.log 2>&1
 	RETVAL=$?
 	[ $RETVAL -eq 0 ] && echo "[OK]"
 	[ $RETVAL -ne 0 ] && echo "[!FAILED!]" && continue
 	
 	echo -n "Moving Installer ...  "
  	mv ./pdf-over-gui/target/$TARGET_FILE $PUBLISH_DIR/$INSTALLER
  	RETVAL=$?
 	[ $RETVAL -eq 0 ] && echo "[OK]"
 	[ $RETVAL -ne 0 ] && echo "[!FAILED!]"
done

echo -n "Building JNLP ...  "

cp ./pdf-over-gui/src/main/jnlp/pdfover.jnlp $PUBLISH_DIR/pdfover.jnlp
RETVAL=$?
if [ $RETVAL -ne 0 ]; then
	echo "[!FAILED!]"
else
	sed -i "s/##CODEBASE_URL##/$CODEBASE_URL/g" $PUBLISH_DIR/pdfover.jnlp
	RETVAL=$?
	if [ $RETVAL -ne 0 ]; then 
		echo "[!FAILED!]"
	else
		sed -i "s/##HOMEPAGE_URL##/$HOMEPAGE_URL/g" $PUBLISH_DIR/pdfover.jnlp
		RETVAL=$?
		if [ $RETVAL -ne 0 ]; then
			echo "[!FAILED!]"
		else
			[ $RETVAL -eq 0 ] && echo "[OK]"
		fi
	fi
fi





