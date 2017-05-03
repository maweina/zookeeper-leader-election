zookeeper-leader-election
=========================

Leader election using the Curator recipes with Zookeeper.

### Build
This project has set up Gradle wrapper, whcih allows you to run a Gradle task without requiring that Gradle is installed.
You can create the distribution(rpm, tar and zip packages) from the root of the project.
```
[root@hadoop1 zookeeper-leader-election]# ./gradlew clean build buildRpm
:clean
:compileJava
:processResources UP-TO-DATE
:classes
:jar
:startScripts
:distTar
:distZip
:assemble
:compileTestJava UP-TO-DATE
:processTestResources UP-TO-DATE
:testClasses UP-TO-DATE
:test UP-TO-DATE
:check UP-TO-DATE
:build
:buildRpm

BUILD SUCCESSFUL

Total time: 13.444 secs
[root@hadoop1 zookeeper-leader-election]# rpm -qpl build/distributions/occimon-1.0.0.0.x86_64.rpm
/opt/occimon
/opt/occimon/bin
/opt/occimon/bin/occimon
/opt/occimon/bin/occimon.bat
/opt/occimon/lib
/opt/occimon/lib/curator-client-2.7.0.jar
/opt/occimon/lib/curator-framework-2.7.0.jar
/opt/occimon/lib/curator-recipes-2.7.0.jar
/opt/occimon/lib/guava-16.0.1.jar
/opt/occimon/lib/jline-0.9.94.jar
/opt/occimon/lib/log4j-1.2.16.jar
/opt/occimon/lib/netty-3.7.0.Final.jar
/opt/occimon/lib/slf4j-api-1.7.21.jar
/opt/occimon/lib/slf4j-simple-1.7.21.jar
/opt/occimon/lib/zookeeper-3.4.6.jar
/opt/occimon/lib/zookeper-leader-election-1.0.0.0.jar
```

### Usage

```
USAGE: Main zkConnectStr znode program [args ...]
```

* zkConnectStr: zookeeper servers address, e.g., "node1:2181,node2:2181,node3:2181"
* znode: zookeeper node path must start with / character
* program [args ...]: the program will be run as child process when I am leader; i will exit if my child process exits.

### Run OCCI Monitor

```
[root@hadoop1 zookeeper-leader-election]# cd build/distributions/
[root@hadoop2 distributions]# unzip occimon-1.0.0.0.zip
Archive:  occimon-1.0.0.0.zip
   creating: occimon-1.0.0.0/
   creating: occimon-1.0.0.0/lib/
  inflating: occimon-1.0.0.0/lib/zookeper-leader-election-1.0.0.0.jar
  inflating: occimon-1.0.0.0/lib/curator-recipes-2.7.0.jar
  inflating: occimon-1.0.0.0/lib/slf4j-simple-1.7.21.jar
  inflating: occimon-1.0.0.0/lib/curator-framework-2.7.0.jar
  inflating: occimon-1.0.0.0/lib/zookeeper-3.4.6.jar
  inflating: occimon-1.0.0.0/lib/guava-16.0.1.jar
  inflating: occimon-1.0.0.0/lib/slf4j-api-1.7.21.jar
  inflating: occimon-1.0.0.0/lib/curator-client-2.7.0.jar
  inflating: occimon-1.0.0.0/lib/log4j-1.2.16.jar
  inflating: occimon-1.0.0.0/lib/jline-0.9.94.jar
  inflating: occimon-1.0.0.0/lib/netty-3.7.0.Final.jar
   creating: occimon-1.0.0.0/bin/
  inflating: occimon-1.0.0.0/bin/occimon
  inflating: occimon-1.0.0.0/bin/occimon.bat
[root@hadoop1 distributions]# occimon-1.0.0.0/bin/occimon "hadoop1:2181,hadoop2:2181" "/test" /opt/logstash/bin/logstash -e 'input { stdin { } } output { stdout {} }
```

### Cases
* Start child process (logstash) if I am leader; give up leadership if failed to start child
* If child is dead, give up leadership. My peers will elect a new leader.
* If my connection to zookeeper is lost or suspended, kill child and give up leadership
* If reconnected to zookeeper, I am still leader candidate
