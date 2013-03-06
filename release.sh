#!/bin/bash

export PRJ=$(cd `dirname $0`; pwd)

if [ $# != 2 ]; then
    echo "usage: release.sh <release-version> <next-trunk-version>"
    echo "(Don't use a -SNAPSHOT suffix in the versions.)"
    exit 1
fi

RELVER=${1%-SNAPSHOT}; shift
NEXTVER=${1%-SNAPSHOT}; shift

export HOME="$WORKSPACE"

x=${GIT:=git}

x=${MAVEN_HOME:=/auto/surf-tp/configs/surf/tools/apache-maven-3.0.3}
export MAVEN_HOME

x=${JAVA_HOME:=/auto/java/jdks/lnx/jdk1.6.0_latest}
export JAVA_HOME

x=${MAVEN_OPTS:=-Xmx4000m -Xms1024m -XX:MaxPermSize=128m}
export MAVEN_OPTS

x=${MVN_OPTIONS:=-e -B --fail-at-end --update-snapshots}
x=${MVN_GOAL:=install}

umask 0022

export MVN="$MAVEN_HOME/bin/mvn"
MVN_REPO="$PRJ/.repository"
mkdir -p "$MVN_REPO"
rm -rf "$MVN_REPO/com/cisco" "$MVN_REPO/org/activiti"
MVN="$MVN -D=maven.repo.local=$MVN_REPO"

cd $PRJ

set -e
set -x

$GIT checkout -f -b v$RELVER
./update-versions.sh $RELVER
$GIT commit -am "created release $RELVER"
$MVN $MVN_OPTIONS clean install -Djava.awt.headless=true || exit 1

$GIT checkout -f master
./update-versions.sh $NEXTVER-SNAPSHOT
$GIT commit -am "new trunk $NEXTVER"
$MVN $MVN_OPTIONS clean install -Dmaven.test.skip=true || exit 1

$GIT checkout -f v$RELVER
$MVN $MVN_OPTIONS -f jenkow-plugin/pom.xml deploy -Dmaven.test.skip=true || exit 1
$MVN $MVN_OPTIONS -f jenkow-activiti-designer/pom.xml deploy -Dmaven.test.skip=true || exit 1
$MVN $MVN_OPTIONS -f jenkow-activiti-explorer/pom.xml deploy -Dmaven.test.skip=true || exit 1
$GIT checkout -f master

$GIT push --all
