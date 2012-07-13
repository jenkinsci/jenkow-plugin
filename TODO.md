* 20120713-01: make designer extension part of Activiti Designer, so no need to install a separate designer extension
  [Activiti Form Thread](http://forums.activiti.org/en/viewtopic.php?f=8&t=4234)
    * It may be possible to completely do away with the User Library, by bundling the extension with Designer
* 20120613-01: Workflow Job Type
    * has one workflow definition
    * historic processes map to build results
    * should not occupy an executor
    * configurable: should allow multiple parallel executions
    * what to show in build log?
* 20120509-01: provide remote access to workflow repository on the server; options: [Git](http://stackoverflow.com/questions/6468122/how-to-write-or-package-a-git-server-as-a-java-servlet-or-java-webapp), [Eclipse EFS](http://www.eclipsezone.com/articles/efs/)?
* 20120504-01: when a Jenkins Task references a non-existing job, create a basic job from template
* 20120504-03: Jenkins Task Type: allow to set Jenkins Job parameter
* 20120504-04: populate workflow execution context with job environment variables (that includes job parameters)
* 20120504-05: populate job environment with workflow attributes (name of workflow, workflow arguments, etc.)
* 20120504-06: workflow persistency (how to persist workflow state on disk to survive Jenkins restarts)
* 20120504-07: How to expose workflow diagram in Jenkins UI?
* 20120504-08: Integrate Activiti Web UIs into Jenkins?
* 20120504-09: Jenkow Designer Perspective for Eclipse
* 20120606-01: can the designer extension Jar file sit physically in the workflow repository project?
