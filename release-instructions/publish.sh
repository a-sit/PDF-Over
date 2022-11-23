#!/bin/bash

#### CONFIGURE: ######
LOG_DIR="/tmp/pdfover_log"

#### DON'T CONFIGURE ####
BASEDIR="$(dirname $(dirname $0))"
PUBLISH_DIR="$BASEDIR/pdf-over-build"

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

begin_phase "Cleaning..."
mvn -B clean > "$LOG_DIR/clean.log" 2>&1
RETVAL=$?
if [ $RETVAL -eq 0 ]; then
	end_phase "OK"
else
	end_phase "FAILED"
fi

profiles=( linux windows mac mac-aarch64 )
if [[ "$1" != "" ]] && [[ "$1" == "--profiles" ]]; then
	profiles=( $2 )
	shift
	shift
	MVN_PARAMS="$@"
fi

pids=()
for (( i = 0 ; i < ${#profiles[@]} ; i++ )) do
	PROFILE=${profiles[$i]}
	begin_phase "Building profile [$PROFILE]..."
	mvn -B install -P$PROFILE -Dno-native-profile $MVN_PARAMS > "$LOG_DIR/build_$PROFILE.log" 2>&1
	RETVAL=$?
	if [ ${RETVAL} -eq 0 ]; then
		end_phase "OK"
	else
		end_phase "FAILED"
	fi
done

popd > /dev/null
