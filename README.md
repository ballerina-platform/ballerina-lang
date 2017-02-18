# Docerina

Docerina is the API documentation generator tool of the Ballerina language. It currently supports generating API 
documentation in HTML format and it can be extended to support additional output formats as required.


## Generating Ballerina API Documentation

Docerina is distributed with [Ballerina Tools Distribution](https://github.com/ballerinalang/tools-distribution) and 
[Docerina Maven Plugin](https://github.com/ballerinalang/plugin-maven/tree/master/docerina-maven-plugin). Instructions
 for generating Ballerina API documentation can be found in above links.
 
 
## Setting up a Docerina Development Environment

Follow below steps to setup a Docerina development environment:

- Clone the Ballerina Git repository:
  
  ```
  git clone https://github.com/ballerinalang/ballerina
  ```
  
- Build Ballerina and extract the distribution:
 
  ```
  cd ballerina
  mvn clean install
  cd modules/distribution/target/
  unzip ballerina-<version>.zip
  cd ballerina-<version> # Refer this as [ballerina-home]
  ```

- Clone the Docerina Git repository:
  
  ```
  cd ..
  git clone https://github.com/ballerinalang/docerina.git
  ```

- Build Docerina and copy the Docerina JAR file to the Ballerina distribution lib folder:
  
  ```
  cd docerina
  mvn clean install
  cd target
  cp docerina-<version>.jar [ballerina-home]/bre/lib/
  ```

- Navigate to Ballerina bin directory and execute the bellow command to generate API documentation:
  
  ```
  cd [ballerina-home]/bin
  ./ballerina doc [ballerina-package-path] # absolute file path of the ballerina source package
  ```
  
- Execute the below command to attach a remote debugging session to debug Docerina:

  ```
  cd [ballerina-home]/bin
    ./ballerina --debug 5005 doc [ballerina-package-path] # absolute file path of the ballerina source package
  ```