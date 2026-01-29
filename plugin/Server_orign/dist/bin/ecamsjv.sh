#JAVA_OPTS="-Xms2048m -Xmx2048m -Xmn786m -verbose:gc"
#JAVA_OPTS="-server -Xms2048m -Xmx2048m -Xmn1024m -verbose:gc"
JAVA_OPTS="-server -Xms2048m -Xmx2048m -Xmn1024m -XX:+UseParallelGC -XX:+UseAdaptiveSizePolicy -verbose:gc -d64"
cygwin=false
os400=false
darwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
Darwin*) darwin=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`


#ECAMSJV_HOME=$HOME/ecamsjv
ECAMSJV_HOME=/home/ecamsdev/ecams/ecamsjv
ECAMSJV_PORT=13202


# For OS400
if $os400; then
  # Set job priority to standard for interactive (interactive - 6) by using
  # the interactive priority - 6, the helper threads that respond to requests
  # will be running at the same priority as interactive jobs.
  COMMAND='chgjob job('$JOBNAME') runpty(6)'
  system $COMMAND

  # Enable multi threading
  export QIBM_MULTI_THREADED=Y
fi

# Get standard Java environment variables
if $os400; then
  # -r will Only work on the os400 if the files are:
  # 1. owned by the user
  # 2. owned by the PRIMARY group of the user
  # this will not work if the user belongs in secondary groups
  BASEDIR="$ECAMSJV_HOME"
  . "$ECAMSJV_HOME"/bin/setclasspath.sh
else
  if [ -r "$ECAMSJV_HOME"/bin/setclasspath.sh ]; then
    BASEDIR="$ECAMSJV_HOME"
    . "$ECAMSJV_HOME"/bin/setclasspath.sh
  else
    echo "Cannot find $CATALINA_HOME/bin/setclasspath.sh"
    echo "This file is needed to run this program"
    exit 1
  fi
fi


# For OS400
if $os400; then
  # Set job priority to standard for interactive (interactive - 6) by using
  # the interactive priority - 6, the helper threads that respond to requests
  # will be running at the same priority as interactive jobs.
  COMMAND='chgjob job('$JOBNAME') runpty(6)'
  system $COMMAND

  # Enable multi threading
  export QIBM_MULTI_THREADED=Y
fi

# Add on extra jar files to CLASSPATH
if [ -n "$JSSE_HOME" ]; then
  CLASSPATH="$CLASSPATH":"$JSSE_HOME"/lib/jcert.jar:"$JSSE_HOME"/lib/jnet.jar:"$JSSE_HOME"/lib/jsse.jar
fi
CLASSPATH="$CLASSPATH":"$ECAMSJV_HOME"/lib:"$ECAMSJV_HOME"/conf:"$ECAMSJV_HOME"/bin/eCAMS_Server.jar

if [ -d "$ECAMSJV_HOME/lib" ] ; then
  for f in "$ECAMSJV_HOME"/lib/*.jar
  do
    CLASSPATH=$CLASSPATH:$f
  done
fi

echo "CLASSPATH: $CLASSPATH"


if [ -z "$ECAMSJV_TMPDIR" ] ; then
  # Define the java.io.tmpdir to use for Catalina
  ECAMSJV_TMPDIR="$ECAMSJV_HOME"/temp
fi

# Bugzilla 37848: When no TTY is available, don't output to console
have_tty=0
if [ "`tty`" != "not a tty" ]; then
    have_tty=1
fi


# ----- Execute The Requested Command -----------------------------------------

# Bugzilla 37848: only output this if we have a TTY
if [ $have_tty -eq 1 ]; then
  echo "Using ECAMSJV_HOME:   $ECAMSJV_HOME"
  echo "Using ECAMSJV_TMPDIR: $ECAMSJV_TMPDIR"
  if [ "$1" = "debug" -o "$1" = "javac" ] ; then
    echo "Using JAVA_HOME:       $JAVA_HOME"
  else
    echo "Using JRE_HOME:       $JRE_HOME"
  fi
fi


ECAMSJV_PID="$ECAMSJV_TMPDIR"/ecamsjv.pid

if [ "$1" = "start" ] ; then
  shift
  touch /home/ecamsdev/ecams/logs/jvlog/ecamsjv.out_`date +%Y%m%d`
    "$_RUNJAVA" $JAVA_OPTS \
      -Decamsjv.port="$ECAMSJV_PORT" \
      -classpath "$CLASSPATH" \
      app.Ecams_Server \
      >> /home/ecamsdev/ecams/logs/jvlog/ecamsjv.out_`date +%Y%m%d` 2>&1 &
      #>> "$ECAMSJV_HOME"/logs/ecamsjv_`date +%Y%m%d`.out 2>&1 &

      if [ ! -z "$ECAMSJV_PID" ]; then
        echo $! > $ECAMSJV_PID
      fi

elif [ "$1" = "stop" ] ; then

  shift

  if [ ! -z "$ECAMSJV_PID" ]; then
     echo "Killing: `cat $ECAMSJV_PID`"
     kill -9 `cat $ECAMSJV_PID`
     rm $ECAMSJV_PID
  else
     echo "Kill failed: \$ECAMSJV_PID not set"
  fi
else

  echo "Usage: ecamsjv.sh ( commands ... )"
  echo "commands:"
  echo "  start             Start Catalina in a separate window"
  echo "  stop              Stop Catalina"
  echo "  stop -force       Stop Catalina (followed by kill -KILL)"
  exit 1

fi