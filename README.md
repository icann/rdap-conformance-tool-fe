ICANN RDAP Conformance Tool Front End
======================================

This is the front end for the RDAP Conformance Tool (https://github.com/icann/rdap-conformance-tool)

# Install and Run
It is meant to be currently deployed as a Docker Container, all you will need is the Dockerfile.
Copy the Dockerfile somewhere and build it:

  `docker build -t rdapctfe .`

Then run it:

  `docker run -p 8080:8080 rdapctfe`


# License

 Copyright 2024 Internet Corporation for Assigned Names and Numbers ("ICANN")

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 

# Acknowledgements

The RDAP Conformance Tool Front End has been developed by Cobenian
(Adam Guyot and Bryan Weber) under a contract from ICANN.