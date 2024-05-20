ICANN RDAP Conformance Tool Front End
======================================

This is the front end for the RDAP Conformance Tool (https://github.com/icann/rdap-conformance-tool)

It is meant to be deployed as a standalone JAR file or as a Docker Container.
To run as a container, first build it:

  docker build -t rdapctfe .

Then run it:

  docker run -p 8080:8080 rdapctfe

