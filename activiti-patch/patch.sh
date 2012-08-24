#!/bin/bash

export PRJ="$(cd `dirname $0`; pwd)"

mkdir -p $PRJ/target

cd $PRJ/target
rm -rf activiti
svn co http://svn.codehaus.org/activiti/projects/designer/trunk/ activiti
cd activiti
patch -p0 -i $PRJ/src/main/resources/activiti-designer-jenkow.diff

mvn -B -f org.activiti.designer.parent/pom.xml clean install
