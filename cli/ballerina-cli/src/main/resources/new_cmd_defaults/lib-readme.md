# Package Overview

This package provides a simple function to print "Hello, World!" to the command line. Additionally, you can provide a name to the function to print a personalized greeting.


## Functionality

### hello Function

```ballerina
public function hello(string? name) returns string {
    if name !is () {
        return string `Hello, ${name}`;
    }
    return "Hello, World!";
}
```

## Build from Source and Publish

### Build the Package

To build the package, use the following command:

```bash
bal pack
```

### Publish the Package

To publish the package to a local repository, use the following command:

```bash
bal push --repository=local
```

## Usage

### Importing the Package

```ballerina
import ballerina/io;
import user/socialMedia;
```

### Using the hello Function

```ballerina
public function main() {
    string greeting = myorg:hello("Ballerina");
    io:println(myorg:hello("Ballerina")); // Output: Hello, Ballerina
}
```

You can call the `hello` function without any arguments to get the default greeting:

```ballerina
public function main() {
    string greeting = socialMedia:hello();
    io:println(greeting); // Output: Hello, World!
}
```
