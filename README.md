# Jenkow Plugin
[Wiki](https://wiki.jenkins-ci.org/display/JENKINS/Jenkow+Plugin),
[CI Build](https://buildhive.cloudbees.com/job/jenkinsci/job/jenkow-plugin/),
[Maven Repository](http://maven.jenkins-ci.org:8081/content/repositories/releases/com/cisco/step/jenkins/plugins/jenkow-plugin/)

## Caveats

* Jenkins task unable to launch a Maven project.  Jenkins task currently launches only items of type [Project](http://javadoc.jenkins-ci.org/hudson/model/Project.html), but a [MavenModuleSet](http://javadoc.jenkins-ci.org/hudson/maven/MavenModuleSet.html) is not a Project, but just an [AbstractProject](http://javadoc.jenkins-ci.org/hudson/model/AbstractProject.html).
