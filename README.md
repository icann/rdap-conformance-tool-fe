ICANN RDAP Conformance Tool Front End
======================================

This is the front end for the RDAP Conformance Tool (https://github.com/icann/rdap-conformance-tool)

## Conformance Tool Dependancy
You will first need to download the modified Cobenian version of the conformance tool at (https://github.com/Cobenian/rdap-conformance-tool) and build the jar:
  `mvn package -DskipTests`

Then install it:
  `mvn install:install-file -Dfile=./tool/target/rdapct-1.0.2.jar -DgroupId=org.icann -DartifactId=rdap-conformance -Dversion=1.0.2 -Dpackaging=jar`


## Build Front End

Buid as follows
  `mvn clean install && mvn package`

This will build the jar locally as:
  `target/rdapctfe-1.0-SNAPSHOT.ja`

It is meant to be deployed as a standalone JAR file or as a Docker Container.
To run as a container, first build it:

  `docker build -t rdapctfe .`

Then run it:

  `docker run -p 8080:8080 rdapctfe`

