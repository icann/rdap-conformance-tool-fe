#!/bin/bash

# Extract the version number from the filename
VERSION=$(echo $1 | awk -F- '{print $2}' | awk -F. '{print $1"."$2"."$3}')

# Define the old and new dependency strings
OLD_DEPENDENCY='<dependency>\n      <groupId>org.icann</groupId>\n      <artifactId>rdap-conformance</artifactId>\n      <version>.*</version>\n    </dependency>'
NEW_DEPENDENCY="<dependency>\n      <groupId>org.icann</groupId>\n      <artifactId>rdap-conformance</artifactId>\n      <version>$VERSION</version>\n    </dependency>"

# Replace the old dependency with the new one in the pom.xml file
sed -i "s|$OLD_DEPENDENCY|$NEW_DEPENDENCY|g" /rdap-conformance-tool-fe/pom.xml