# Docerina

Docerina is the API documentation generator tool of the Ballerina language. 

## How to run

Follow below steps to generate Ballerina API documentation using Docerina:

- Clone the Ballerina Git repository:
  
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

- Navigate to the Docerina bin directory and execute the below command 
to generate API documentation for Ballerina source code:
  
  ```
  cd docerina-<version>/bin
  ./docerina [ballerina-package-path] # absolute file path of the ballerina source package
  ```