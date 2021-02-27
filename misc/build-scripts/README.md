# Introduction

This Jenkins pipeline script automates the build/release process of 
Ballerina repositories. This builds ballerina-parent, ballerina, 
composer, connectors, container-support, docerina, tool-openapi-ballerina
and tools-distribution in the order they depend on each other while 
updating the dependency versions.

# Usage

* Install Pipeline Plugin[1], Pipeline Groovy Plugin[2].
There are some required dependencies for these two plugins 
but they will be automatically installed in Jenkins.
Also, Config File Provider Plugin[3] and Environment Injector Plugin[4],
Credentials Plugin[5] need to be installed.

* Now, go to Jenkins Dashboard -> Credentials and add your git credentials.
Please note that you need to have commit rights to ballerina repositories
in order to perform a release. 

* Also, go to Jenkins Dashboard -> Manage Jenkins -> Managed Files and add
your settings.xml which is used to perform the release/build.
You need to have local m2 repository and scm server configured in this.
Please note that, since this script is written NOT to deploy artifacts 
to nexus, in order for the build/release process to work as expected,
all repositories should be using the same local m2 repository.

* Now go to Jenkins Dashboard -> Manage Jenkins -> Global Tools Configuration
and add a JDK 11 installation and an appropriate Maven installation.

* Now, go to Jenkins Dashboard -> New Item and create a Pipeline Job. 
Then you will be directed to configuration section of the job,
There, tick "Prepare an environment for the run" and in the 
Properties Content section, enter following properties in KEY=VALUE 
format.

GIT_CREDENTIALS_ID - This is the id of the git credentials you added  
GIT_EMAIL - This is the email of the git user associated with the above credentials  
SETTINGS_XML_ID - This is the id of the settings.xml you added  
MAVEN_TOOL=One of the appropriate maven installations you added  
JDK - JDK 11 installation you added  
GIT_USER - ballerinalang. (We are cloning repositories from ballerinalang  
organization. This can be changed as needed)
 
eg:  
GIT_CREDENTIALS_ID=git-credentials-id  
GIT_EMAIL=ballerinalang@wso2.com  
SETTINGS_XML_ID=adb07180-fa9c-4bea-94c4-130813b8018c  
MAVEN_TOOL=apache-maven-3.3.9  
JDK=Oracle-JDK-11  
GIT_USER=ballerinalang  

Save your configuration.

* Now you can new job you created and click "Build Now". 
Once build begings it will request you to choose whether you need to do
a Build or a Release and whether the workspace needs to be cleaned or not.
Then depending on your previous choice you need to enter some more inputs
and proceed.


[1] https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Plugin  
[2] https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Groovy+Plugin  
[3] https://wiki.jenkins-ci.org/display/JENKINS/Config+File+Provider+Plugin  
[4] https://wiki.jenkins-ci.org/display/JENKINS/EnvInject+Plugin   
[5] https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Plugin
