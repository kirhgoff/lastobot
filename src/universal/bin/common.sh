#!/bin/bash

export JAVA_HOME=$JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

SCRIPT=$(readlink -f "$0")
BINDIR=$(dirname "$SCRIPT")
HOME=$(dirname "$BINDIR")
LOGDIR=${HOME}/logs
PID_FILE=${HOME}/work/${INSTANCE_ID}.pid
LOG=${LOGDIR}/${INSTANCE_ID}

# parse command line
OPTIND=1
profile=0
debug=0
verbose=0
while getopts "pdv" opt; do
    case "$opt" in
    v)  verbose=1
        ;;
    d)  debug=1
        ;;
    p)  profile=1
        ;;
    esac
done
shift $((OPTIND-1))

COMMAND_NAME=$1

function log {
    if [[ $verbose -eq 1 ]]; then
        echo "$(date --rfc-3339=seconds) $@"
    fi
}

function check_alive {
    if [ -f "$PID_FILE" ] ; then
        PROCESS_PID=$(cat $PID_FILE)
        log "Found PID_FILE $PID_FILE, checking PID $PROCESS_PID"
        ps -p $PROCESS_PID > /dev/null
        if [ $? -eq 0 ] ; then
            log "Found process with PID $PROCESS_PID"
            return 1
        fi
        log "Process with PID $PROCESS_PID is not running"
    else
        log "PID_FILE $PID_FILE does not exist"
    fi

    if [[ -z "$EXECUTABLE" ]] ; then
        PNAME="java"
    else
        PNAME="$EXECUTABLE"
    fi

    PROCESS_PID="$(ps eauxww | grep ENVIRONMENT_ID=${ENVIRONMENT_ID} | grep INSTANCE_ID=${INSTANCE_ID} | grep $PNAME | awk '{ print $2 }')"
    log "Checking process (ENVIRONMENT_ID=${ENVIRONMENT_ID}, INSTANCE_ID=${INSTANCE_ID}, PNAME=${PNAME}): PID=$PROCESS_PID"
    if [ -n "$PROCESS_PID" ]; then
        log "Pid file is missing but process ${INSTANCE_ID} seems to be running, PID $PROCESS_PID"
        return 1
    fi
    PROCESS_PID=""
    return 0
}

function compress_logs {
   LOG_FILES_PATTERN="${LOGDIR}/$1"
   # find all log files and get last modified time
   LATEST_LOG_TIMESTAMP=$(ls -1 $LOG_FILES_PATTERN | xargs stat -c=%y | sort | tail -1)
   # pretty print last modified time to yyyyMMdd_hhmmss
   TIMESTAMP=$(echo $LATEST_LOG_TIMESTAMP | sed 's/[-=:]//g' | sed 's/ /_/' | cut -b 1-15)

   for logfile in $LOG_FILES_PATTERN; do
       old_file_name=$(basename "$logfile")
       new_file_name=$(echo $old_file_name | sed "s/\.log$/.${TIMESTAMP}.log/")
       mkdir -p "$(dirname $logfile)/archive"
       newfile="$(dirname $logfile)/archive/$new_file_name"
       log "renaming $logfile to $newfile"
       mv "$logfile" "$newfile"
       nohup gzip "$newfile" &> /dev/null < /dev/null &
   done
}

function delete_old_logs {
   log "deleting old logs from ${LOGDIR}"
   find ${LOGDIR}/archive -type f -mtime +7 -exec rm -rf {} \;
}

function check_disown {
    MAX_RETRIES=30
    while [[ $(ps -p ${PROCESS_PID} -o tty=) != '?' && $MAX_RETRIES -ne 0 ]]
    do
      sleep 1
      MAX_RETRIES=$[MAX_RETRIES-1]
    done

    if [ $MAX_RETRIES -eq 0 ]; then
      log "Coud't disown process"
      exit 1
    fi
}

case "$COMMAND_NAME" in
start)
    check_alive
    if [ -n "$PROCESS_PID" ]; then
        echo "Process ${INSTANCE_ID} is already running, PID $PROCESS_PID"
        exit 1
    fi
    compress_logs "${INSTANCE_ID}*.log"
    if [[ -n "$EXTRA_LOGS" ]] ; then
        arr=$(echo $EXTRA_LOGS | tr "," "\n")
        for X in $arr
        do
            compress_logs "$X"
        done
    fi

    delete_old_logs

    echo "Starting $INSTANCE_ID..."

    cd $HOME
    mkdir -p ${HOME}/logs
    mkdir -p ${HOME}/work

    if [[ -z "$EXECUTABLE" ]]
    then
        # Prepare class path
        CLASSPATH="$HOME/config:${HOME}/lib/*:$CLASSPATH"
        log "CLASSPATH=$CLASSPATH"

        # Java Options
        CORE_JAVA_OPTS="-server -showversion -Dfile.encoding=utf-8"
        log "CORE_JAVA_OPTS=$CORE_JAVA_OPTS"

        if [[ -z "$MEM_OPTS" ]]; then
            MEM_OPTS="-Xmx4g -Xms4g"
        fi
        MEM_OPTS="$MEM_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOGDIR}"
        log "MEM_OPTS=$MEM_OPTS"

        if [[ -z "$GC_OPTS" ]]; then
            GC_OPTS="-XX:+UseG1GC"
        fi
        GC_OPTS="$GC_OPTS -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -Xloggc:${LOGDIR}/${INSTANCE_ID}-gc.log"
        log "GC_OPTS=$GC_OPTS"

        if [[ -z "$DEBUG_PORT" ]]; then
            DEBUG_PORT="5000"
        fi
        if [[ -z "$DEBUG_OPTS" ]]; then
            DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=$DEBUG_PORT"
        fi
        log "DEBUG_OPTS=$DEBUG_OPTS"

        if [[ -z "$PROFILE_OPTS" ]]; then
            PROFILE_OPTS="-agentpath:/opt/yourkit/bin/linux-x86-64/libyjpagent.so"
        fi
        log "PROFILE_OPTS=$PROFILE_OPTS"

        if [[ -z "$JMX_PORT" ]]; then
            JMX_PORT="0"
        fi
        if [[ -z "$JMX_OPTS" ]]; then
            jmxRemote="-Dcom.sun.management.jmxremote"
            JMX_OPTS="${jmxRemote} ${jmxRemote}.port=${JMX_PORT} ${jmxRemote}.local.only=false ${jmxRemote}.authenticate=false ${jmxRemote}.ssl=false"
        fi
        log "JMX_OPTS=$JMX_OPTS"

        JAVA_OPTS="$CORE_JAVA_OPTS $MEM_OPTS $GC_OPTS $JMX_OPTS $SECURITY_OPTS $JAVA_AGENT"
        if [[ $profile -eq 1 ]]; then
            JAVA_OPTS="$JAVA_OPTS $PROFILE_OPTS"
        fi
        if [[ $debug -eq 1 ]]; then
            JAVA_OPTS="$JAVA_OPTS $DEBUG_OPTS"
        fi

        log "JAVA_OPTS=$JAVA_OPTS"
        CMD_LINE="java ${JAVA_OPTS} -cp ${CLASSPATH} ${MAIN_CLASS} ${MAIN_ARGS}"
    else
        if [[ -z "${EXECUTABLE_HOME}" ]]; then
            EXECUTABLE_HOME="${BINDIR}"
        fi
        CMD_LINE="${EXECUTABLE_HOME}/${EXECUTABLE} ${EXECUTABLE_OPTS}"
    fi

    # Run process
    log "CMD_LINE=${CMD_LINE}"
    ENVIRONMENT_ID=${ENVIRONMENT_ID} INSTANCE_ID=${INSTANCE_ID} setsid ${CMD_LINE} > ${LOGDIR}/${INSTANCE_ID}-stdout.log 2>&1& disown -h

    if [ $? -ne 0 ] ; then
        echo "Process failed to start"
        exit 1
    fi

    PROCESS_PID=$!
    check_disown

    echo "Process started, pid=$PROCESS_PID, saving pid to $PID_FILE"
    echo "$PROCESS_PID" > $PID_FILE
    log "PID_FILE=$PID_FILE, contents=$(cat $PID_FILE)"
    exit 0
    ;;

stop)
    check_alive
    if [ -n "$PROCESS_PID" ]; then
        echo "Stopping $INSTANCE_ID, pid=$PROCESS_PID..."
        kill $PROCESS_PID
        for i in $(seq 1 11); do
            check_alive
            if [ -z "$PROCESS_PID" ]; then
                echo "Stopped."
                rm -f $PID_FILE
                exit 0
            fi
            sleep 1
        done
        echo "Terminating $INSTANCE_ID, pid=$PROCESS_PID"
        kill -9 $PROCESS_PID
        rm -f $PID_FILE
        exit 0
    else
        echo "$PROCESS_PID not found, nothing to stop."
    fi
	;;

status)
    check_alive
    if [ -n "$PROCESS_PID" ]; then
        echo "Process is running with PID $PROCESS_PID"
        exit 0
    else
        echo "Process is not running"
        exit 1
	fi
	;;

*)
    echo "Available commands are start|stop|status"
    ;;

esac
