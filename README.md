# Jenkow Plugin
[Wiki](https://wiki.jenkins-ci.org/display/JENKINS/Jenkow+Plugin),
[CI Build](https://buildhive.cloudbees.com/job/jenkinsci/job/jenkow-plugin/),
[Maven Repository](http://maven.jenkins-ci.org:8081/content/repositories/releases/com/cisco/step/jenkins/plugins/jenkow-plugin/)

## Build Instructions

Until the Jenkow enhancements to the Activiti Designer is in place \(see [Activiti Forum: bundling Designer Extension with Designer?](http://forums.activiti.org/en/viewtopic.php?f=8&t=4234)\), the Jenkow plugin project builds its own Activiti Designer and the build needs to be done in two steps: "mvn package; mvn install"
