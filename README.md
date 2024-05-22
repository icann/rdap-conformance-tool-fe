ICANN RDAP Conformance Tool Front End
======================================

This is the front end for the RDAP Conformance Tool (https://github.com/icann/rdap-conformance-tool)


It is meant to be currently deployed as a Docker Container, all you will need is the Dockerfile.
Copy the Dockerfile somewhere and build it:

  `docker build -t rdapctfe .`

Then run it:

  `docker run -p 8080:8080 rdapctfe`

