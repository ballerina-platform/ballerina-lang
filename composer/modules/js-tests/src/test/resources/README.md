1. For MAC, install coreutils and replace cp with gcp (brew install coreutils)
2. `rm -rf composer/modules/js-tests/src/test/resources/passing`
3. `mkdir composer/modules/js-tests/src/test/resources/passing`
4. `rm -rf composer/modules/js-tests/src/test/resources/failing`
5. `mkdir composer/modules/js-tests/src/test/resources/failing`
6. copy files from bal-by-example

`find docs/ballerina-by-example/examples -name \*.bal | xargs gcp --parents -t ./composer/modules/js-tests/src/test/resources/passing`

7. copy files from core tests | exclude invalid/negative test bal files

`find tests/ballerina-test/src/test/resources/test-src -name \*.bal | grep -v negative | grep -v invalid | xargs gcp --parents -t ./composer/modules/js-tests/src/test/resources/passing`

8. move failing files to failing directory

`mvn clean install -DmoveFailingFiles=true`
