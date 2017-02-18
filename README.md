# Docerina

Docerina is the API documentation generator tool of the Ballerina language. 

## How to run

Follow below steps to generate Ballerina API documentation using Docerina:

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
  git clone https://github.com/wso2/docerina.git
  ```

- Build Docerina and copy the Docerina JAR file to Ballerina distribution lib folder:
  
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