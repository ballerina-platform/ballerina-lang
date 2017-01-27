# Docerina

Docerina is the API documentation generator tool of the Ballerina language. 

## How to run

Follow below steps to generate Ballerina API documentation using Docerina:

1. Clone Docerina git repository:

```
git clone https://github.com/wso2/docerina.git
```

2. Build and extract Docerina distribution:
```
mvn clean install
cd target
unzip docerina-<version>.zip
```

3. Generate Ballerina API documentation:

```
cd docerina-<version>/bin
./docerina [ballerina-package-path]
```