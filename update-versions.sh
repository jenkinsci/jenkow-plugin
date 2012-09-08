#!/bin/bash
# The MIT License
#
# Copyright (c) 2012, Cisco Systems, Inc., Max Spring
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

export PRJ=$(cd `dirname $0`; pwd)

function usageHalt ()
{
    echo "usage: $0 new-version"
    exit 0
}

if [ $# -ne 1 ]; then
    usageHalt
fi

NEW_VERSION=$1; shift

BUNDLE_VERSION=$NEW_VERSION
case $NEW_VERSION in
*-SNAPSHOT) BUNDLE_VERSION=${NEW_VERSION%%-SNAPSHOT}.qualifier ;;
esac

x=${XML:=/usr/cisco/bin/xml}
TS=`date +%Y%m%d-%H%M%S`

function updateAnyVersion ()
{
    local POM="$1"; shift
    local XPATH="$1"; shift

    local PRJDIR="$(cd `dirname $POM`; pwd)"
    local POMNAME="${POM##*/}"
    local POMN="$PRJDIR/target/$POMNAME"

    mkdir -p "$PRJDIR/target"
    
    $XML ed --pf --ps \
        -N ns=http://maven.apache.org/POM/4.0.0 \
        -u "$XPATH" -v "$NEW_VERSION" "$POM" >"$POMN"

    if ! diff "$POM" "$POMN" ; then
        echo "updating $POM"
        mv "$POM" "$PRJDIR/target/$POMNAME.$TS" || exit 1
        mv "$POMN" "$POM"
    else
        rm "$POMN"
    fi
}

function updateParentVersion ()
{
    local POM="$1"; shift

    updateAnyVersion "$POM" "/ns:project/ns:parent/ns:version"
}

function updateMainVersion ()
{
    local POM="$1"; shift

    updateAnyVersion "$POM" "/ns:project/ns:version"
}

function updateBundleVersion ()
{
    local MANIFEST="$1"; shift

    local PRJDIR="$(cd `dirname $MANIFEST`/..; pwd)"
    local MANIFESTN="$PRJDIR/target/MANIFEST.MF"
    mkdir -p "$PRJDIR/target"

    sed \
        -e "s/Bundle-Version: .*\$/Bundle-Version: $BUNDLE_VERSION/g" \
        "$MANIFEST" >"$MANIFESTN"

    if ! diff "$MANIFEST" "$MANIFESTN" ; then
        echo "updating $MANIFEST"
        mv "$MANIFEST" "$PRJDIR/target/MANIFEST.MF.$TS" || exit 1
        mv "$MANIFESTN" "$MANIFEST"
    else
        rm "$MANIFESTN"
    fi
}

function updateEclipseFeatureVersion ()
{
    local FEATURE="$1"; shift

    local PRJDIR="$(cd `dirname $FEATURE`; pwd)"
    local FEATUREN="$PRJDIR/target/feature.xml"

    mkdir -p "$PRJDIR/target"
    
    $XML ed --pf --ps \
        -u "/feature/@version" -v "$BUNDLE_VERSION" "$FEATURE" >"$FEATUREN"

    diff "$FEATURE" "$FEATUREN"
    if ! diff -q "$FEATURE" "$FEATUREN" >/dev/null ; then
        echo "updating $FEATURE"
        mv "$FEATURE" "$PRJDIR/target/$FEATURENAME.$TS" || exit 1
        mv "$FEATUREN" "$FEATURE"
    else
        rm "$FEATUREN"
    fi
}

updateMainVersion "$PRJ/pom.xml"

find $PRJ -mindepth 2 -type f -name pom.xml \
| grep -v '/org.activiti.designer' \
| grep -v '/target/' \
| while read POM; do
    updateParentVersion "$POM"
done

find $PRJ/eclipse -type f -name MANIFEST.MF \
| grep -v '/org.activiti.designer' \
| grep -v '/target/' \
| while read MANIFEST; do
    updateBundleVersion "$MANIFEST"
done

find `pwd` -type f -name feature.xml \
| grep -v '/org.activiti.designer' \
| grep -v '/target/' \
| while read FEATURE; do
    updateEclipseFeatureVersion "$FEATURE"
done
