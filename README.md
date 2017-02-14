# Docerina

Docerina is the API documentation generator tool of the Ballerina language. 

## How to run

Follow below steps to generate Ballerina API documentation using Docerina:

- Clone the Ballerina core Git repository:
  ```
  git clone https://github.com/ballerinalang/ballerina
  ```
- Build Ballerina:
  ```
  cd ballerina
  mvn clean install
  ```

- Clone the Docerina Git repository:
  ```
  cd ..
  git clone https://github.com/wso2/docerina.git
  ```

- Build and extract the Docerina distribution:
  ```
  cd docerina
  mvn clean install
  cd target
  unzip docerina-<version>.zip
  ```

- Navigate to your Ballerina runtime bin directory, and the generate the Ballerina API documentation as follows:
  ```
  ballerina doc generate [ballerina-source-file-path]
  ```
  where `[ballerina-package-path]` is TODO: give example of the path
