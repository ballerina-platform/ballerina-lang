# Ballerina Container Support

Container support for Ballerina provides the implementation for the following functionality.

1. Package Ballerina programs with Docker using `ballerina docker` command.

## Building
Navigate to the source root and execute the following command.

```bash
mvn clean install
```

This will a fat JAR file with the dependencies included, inside the `target` folder.

## Usage
Add the `ballerina-container-support-<VERSION>.jar` file to `bre/lib/` folder in the Ballerina distribution.

### `ballerina docker` Command Line Usage
TODO

## License
Ballerina Container Support is licensed under [Apache License v2](LICENSE).

