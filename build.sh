#bin/bash

./gradlew clean build -x test -x check
/bin/cp -rf language-server/modules/langserver-core/build/libs/language-server-core-2.0.0-Preview9-SNAPSHOT.jar /home/anjana/repos/module-ballerina-c2c/c2c-ballerina/build/extracted-distribution/jballerina-tools-2.0.0-Preview9-SNAPSHOT/lib/tools/lang-server/lib/
/bin/cp -rf language-server/modules/langserver-commons/build/libs/language-server-commons-2.0.0-Preview9-SNAPSHOT.jar /home/anjana/repos/module-ballerina-c2c/c2c-ballerina/build/extracted-distribution/jballerina-tools-2.0.0-Preview9-SNAPSHOT/lib/tools/lang-server/lib/

/bin/cp -rf language-server/modules/langserver-core/build/libs/language-server-core-2.0.0-Preview9-SNAPSHOT.jar /home/anjana/repos/module-ballerina-c2c/c2c-ballerina/build/extracted-distribution/jballerina-tools-2.0.0-Preview9-SNAPSHOT/bre/lib/
/bin/cp -rf language-server/modules/langserver-commons/build/libs/language-server-commons-2.0.0-Preview9-SNAPSHOT.jar /home/anjana/repos/module-ballerina-c2c/c2c-ballerina/build/extracted-distribution/jballerina-tools-2.0.0-Preview9-SNAPSHOT/bre/lib/

echo "Copied";
