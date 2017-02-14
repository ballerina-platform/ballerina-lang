# Docerina

Docerina is the API documentation generator tool of the Ballerina language. 

## How to run

Follow below steps to generate Ballerina API documentation using Docerina:

- Clone Docerina git repository:

  ```
  git clone https://github.com/wso2/docerina.git
  ```

- Build and extract Docerina distribution:
  ```
  cd docerina
  mvn clean install
  cd target
  unzip docerina-<version>.zip
  ```

- Generate Ballerina API documentation:

  ```
  cd docerina-<version>/bin
  ./docerina [ballerina-package-path]
  ```
