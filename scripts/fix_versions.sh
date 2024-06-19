#!/bin/bash
# Extract the version number from the filename
VERSION=$(echo $1 | awk -F- '{print $2}' | awk -F. '{print $1"."$2"."$3}')

# Define the old and new dependency strings with improved regex for whitespace
OLD_DEPENDENCY='<dependency>\n\s*<groupId>org\.icann</groupId>\n\s*<artifactId>rdap-conformance</artifactId>\n\s*<version>.*</version>\n\s*</dependency>'
NEW_DEPENDENCY="<dependency>\n      <groupId>org.icann</groupId>\n      <artifactId>rdap-conformance</artifactId>\n      <version>$VERSION</version>\n    </dependency>"

# Replace the old dependency with the new one in the pom.xml file, handling any amount of whitespace
sed -i "s|$OLD_DEPENDENCY|$NEW_DEPENDENCY|g" /rdap-conformance-tool-fe/pom.xml