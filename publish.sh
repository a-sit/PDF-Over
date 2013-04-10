#!/bin/bash

#### CONFIGURE: ######
PUBLISH_DIR="/srv/apache2/www/pdf-over"
LOG_DIR="log"
CODEBASE_URL="http:\/\/abyss.iaik.tugraz.at\/pdf-over\/"
CONTEXT_URL="http:\/\/abyss.iaik.tugraz.at\/pdf-over\/"
HOMEPAGE_URL="http:\/\/www.buergerkarte.at"


#### DON'T CONFIGURE ####
BASEDIR="`dirname $0`"

VERSION=`grep -m1 "<version>" "$BASEDIR/pom.xml" | sed -e "s/[ \t]*<version>\(.*\)<\/version>/\1/"`
TARGET_FILE="pdf-over-gui-$VERSION-standard.jar"

TBOLDGRAY="\033[1;30m"
TGREEN="\033[0;32m"
TBOLDGREEN="\033[1;32m"
TYELLOW="\033[0;33m"
TBOLDYELLOW="\033[1;33m"
TRED="\033[0;31m"
TBOLDRED="\033[1;31m"
TNORMAL="\033[0;39m"

COLS=$(tput cols)

MVN_PARAMS="$@"

function begin_phase {
	MSG=$@
	printf "$MSG"
}

function end_phase {
	STATUS=$1
	case "$STATUS" in
		"OK")		STATUSCOLOR="$TBOLDGREEN";;
		"FAILED")	STATUSCOLOR="$TBOLDRED";;
		*)		STATUSCOLOR="$TBOLDYELLOW";;
	esac
	PAD=$(($COLS-${#MSG}))
	printf "%b%${PAD}s%b" "$STATUSCOLOR" "[$STATUS]" "$TNORMAL"
}

pushd $BASEDIR > /dev/null

echo -e "Publishing to: $TYELLOW$PUBLISH_DIR$TNORMAL"
mkdir -p "$PUBLISH_DIR"
mkdir -p "$LOG_DIR"

profiles=( linux windows mac )
names=( linux windows mac )
if [[ "$1" != "" ]] && [[ "$1" == "--profiles" ]]; then
	profiles=( $2 )
	names=( $2 )
	shift
	shift
	MVN_PARAMS="$@"
fi

for (( i = 0 ; i < ${#names[@]} ; i++ )) do
	PROFILE=${profiles[$i]}
	NAME=${names[$i]}
	INSTALLER=setup_pdf-over_$NAME.jar
	begin_phase "Building package [$PROFILE] as $INSTALLER..."
	mvn install -P$PROFILE $MVN_PARAMS > "$LOG_DIR/build_$NAME.log" 2>&1
	RETVAL=$?
	if [ $RETVAL -eq 0 ]; then
		end_phase "OK"
	else
		end_phase "FAILED"
		continue
	fi

	begin_phase "Moving Installer..."
	mv "./pdf-over-gui/target/$TARGET_FILE" "$PUBLISH_DIR/$INSTALLER"
	RETVAL=$?
	if [ $RETVAL -eq 0 ]; then
		end_phase "OK"
	else
		end_phase "FAILED"
	fi
done

begin_phase "Building JNLP..."
cp ./pdf-over-gui/src/main/jnlp/pdf-over.jnlp "$PUBLISH_DIR/pdf-over.jnlp"
RETVAL=$?
if [ $RETVAL -ne 0 ]; then
	end_phase "FAILED"
else
	sed -i "s/##CODEBASE_URL##/$CODEBASE_URL/g" "$PUBLISH_DIR/pdf-over.jnlp"
	RETVAL=$?
	if [ $RETVAL -ne 0 ]; then
		end_phase "FAILED"
	else
		sed -i "s/##CONTEXT_URL##/$CONTEXT_URL/g" "$PUBLISH_DIR/pdf-over.jnlp"
		RETVAL=$?
		if [ $RETVAL -ne 0 ]; then
			end_phase "FAILED"
		else
			sed -i "s/##HOMEPAGE_URL##/$HOMEPAGE_URL/g" "$PUBLISH_DIR/pdf-over.jnlp"
			RETVAL=$?
			if [ $RETVAL -ne 0 ]; then
				end_phase "FAILED"
			else
				end_phase "OK"
			fi
		fi
	fi
fi

begin_phase "Copying images..."
cp -r ./pdf-over-gui/src/main/resources/icons "$PUBLISH_DIR"
RETVAL=$?
if [ $RETVAL -ne 0 ]; then
	end_phase "FAILED"
else
	end_phase "OK"
fi

begin_phase "Building javadoc..."
mvn javadoc:aggregate > "$LOG_DIR/javadoc.log" 2>&1 && cp -r target/site/apidocs/ "$PUBLISH_DIR"
RETVAL=$?
if [ $RETVAL -ne 0 ]; then
	end_phase "FAILED"
else
	end_phase "OK"
fi

popd > /dev/null
