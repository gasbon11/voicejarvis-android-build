#!/bin/sh

# Set default values for memory options
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Attempt to set APP_HOME
APP_HOME=$(cd "`dirname "$0"`" && pwd -P) || exit

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

exec java $DEFAULT_JVM_OPTS -jar "$CLASSPATH" "$@"
