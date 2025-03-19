# Package Overview

This package provides a simple utility function to print "Hello, World!" to the command line. It also supports personalized greetings by optionally accepting a name.

## Build from Source and Publish

### Build the Package

To build the package locally, execute the following command in the terminal:

```bash
bal pack
```

This command compiles the package and prepares it for use or distribution.

### Publish the Package

To publish the package to a local repository, use the following command:

```bash
bal push --repository=local
```

## Usage

To use the user/socialMedia package in your Ballerina project, import it along with the ballerina/io module for output handling.
(Note: For this example, we are assuming the organization name is `user` and the package name is `socialMedia`.)

```ballerina
import ballerina/io;
import user/socialMedia;

public function main() {
    string greeting = socialMedia:hello("Ballerina");
    io:println(greeting); // Output: Hello, Ballerina
}
```

The `hello` function can be called without any arguments to get the default greeting:

```ballerina
public function main() {
    string greeting = socialMedia:hello();
    io:println(greeting); // Output: Hello, World!
}
```
