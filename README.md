ISAAC: Informatics Analytic Architecture
======================

A dynamic semantic architecture for the analysis of models, logic, and language.

## Deploying to a repository
To deploy, set a profile in your settings.xml with the repository you want to deploy to, 
patterned after these entries:



```
  <profile>
      <id>release-deploy</id>
      <properties>
        <altDeploymentRepository>central::default::http://artifactory.isaac.sh/artifactory/libs-release-local</altDeploymentRepository>
      </properties>
  </profile>

  <profile>
      <id>snapshot-deploy</id>
      <properties>
         <altDeploymentRepository>central::default::http://artifactory.isaac.sh/artifactory/libs-snapshot-local</altDeploymentRepository>
      </properties>
  </profile>

```

Then you can deploy to the repository of your choice using the following command:  

mvn clean deploy -Psnapshot-deploy


## Performing a release
```
$ mvn jgitflow:release-start jgitflow:release-finish \
         -DreleaseVersion=3.08 -DdevelopmentVersion=3.09-SNAPSHOT

$ mvn jgitflow:release-start jgitflow:release-finish -Prelease-deploy
```



## FAQ

1) If you are running the build for the first time and it's broken with the following error:

[ERROR] /Users/patrick/s/osehra/ISAAC/core/api/src/main/java/sh/isaac/api/util/HeadlessToolkit.java:[101,33] cannot find symbol
[ERROR]   symbol:   class HitInfo

or

[ERROR] Failed to execute goal on project iconography: Could not resolve dependencies for project sh.isaac.komet:iconography:jar:4.23-SNAPSHOT: The following artifacts could not be resolved: de.jensd:fontawesomefx-commons:jar:8.15, de.jensd:fontawesomefx-controls:jar:8.15, de.jensd:fontawesomefx-emojione:jar:2.2.7-2, de.jensd:fontawesomefx-fontawesome:jar:4.7.0-5, de.jensd:fontawesomefx-icons525:jar:3.0.0-4, de.jensd:fontawesomefx-materialdesignfont:jar:1.7.22-4, de.jensd:fontawesomefx-materialicons:jar:2.2.0-5, de.jensd:fontawesomefx-materialstackicons:jar:2.1-5, de.jensd:fontawesomefx-octicons:jar:4.3.0-5, de.jensd:fontawesomefx-weathericons:jar:2.0.10-5: Could not find artifact de.jensd:fontawesomefx-commons:jar:8.15 in central (https://repo.maven.apache.org/maven2) -> [Help 1]
[ERROR]

This may be the fix.

Check that ~/.m2/settings.xml exists.  Use the settings.xml found here:
https://bitbucket.org/Jerady/fontawesomefx/issues/43/how-to-get-fontawesome-812-from-maven

Next, make sure java is at the latest 1.8.x version.  Also, make sure maven is using the same jdk.  If you are using jenv to have multiple java versions on your machine... be aware jenv does not set the output of /usr/libexec/java correctly.

Example:

# jenv versions
  system
  1.8
* 1.8.0.92 (set by /Users/patrick/.jenv/version)
  9-ea
  oracle64-1.8.0.92
  oracle64-9-ea

# java -version
java version "1.8.0_92"
Java(TM) SE Runtime Environment (build 1.8.0_92-b14)
Java HotSpot(TM) 64-Bit Server VM (build 25.92-b14, mixed mode)

# mvn -version
Apache Maven 3.5.0 (ff8f5e7444045639af65f6095c62210b5713f426; 2017-04-03T13:39:06-06:00)
Maven home: /usr/local/Cellar/maven/3.5.0/libexec
Java version: 9-ea, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk-9.jdk/Contents/Home
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.12.6", arch: "x86_64", family: "mac"

# jenv exec mvn -version
Apache Maven 3.5.0 (ff8f5e7444045639af65f6095c62210b5713f426; 2017-04-03T13:39:06-06:00)
Maven home: /usr/local/Cellar/maven/3.5.0/libexec
Java version: 1.8.0_92, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk1.8.0_92.jdk/Contents/Home/jre
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "10.12.6", arch: "x86_64", family: "mac"


Use "jenv exec mvn clean install" to get maven using the correct jdk.

The first time I attempted to build this app these were the problems I had and creating the proper settings.xml file and running "jenv exec mvn clean install" fixed the problem.


