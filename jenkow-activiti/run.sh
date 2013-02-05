#!/bin/bash

export PRJ="$(cd `dirname $0`; pwd)"

if [ -z "$WORKSPACE" ]; then
    export WORKSPACE="$PRJ"
    CREATE_PRIV_LOCAL_REPO=0
else
    CREATE_PRIV_LOCAL_REPO=1
fi

export HOME="$WORKSPACE"

x=${MAVEN_HOME:=/auto/surf-tp/configs/surf/tools/apache-maven-3.0.3}
export MAVEN_HOME

x=${JAVA_HOME:=/auto/java/jdks/lnx/jdk1.6.0_latest}
export JAVA_HOME

x=${MVN_OPTIONS:=-e -B --fail-at-end --update-snapshots}
x=${MVN_GOAL:=install}

unset DISPLAY
umask 0022

export MVN="$MAVEN_HOME/bin/mvn"
if [ $CREATE_PRIV_LOCAL_REPO -eq 1 ]; then
    MVN_REPO="$PRJ/.repository"
    mkdir -p "$MVN_REPO"
    MVN="$MVN -D=maven.repo.local=$MVN_REPO"
fi

export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4286,server=y,suspend=n -Xmx512m -XX:PermSize=128m"

echo "(export MVN_OPTIONS=\"$MVN_OPTIONS\"; export MVN_GOAL=\"$MVN_GOAL\"; $PRJ/build.sh)"

set -x

cd $PRJ/modules/activiti-webapp-explorer2
$MVN jetty:run
