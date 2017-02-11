# Working with Ballerina v0.8.0-SNAPSHOT

Ballerina is a new programming language for integration built on a sequence diagram metaphor. 

## Working with Ballerina runtime. 

Ballerina provides two runtime modes. 

* Standalone Mode
* Server Mode

### **Standalone Mode** 

Execute the `main` function of a given Ballerina program by providing zero or more arguments. Once `main` function completes its execution, Ballerina runtime will exit.

The Main function is structured as follow.  

```
function main (string[] args) {
    ConnectorDeclaration;*
    VariableDeclaration;*
    Statement;+
}
```

####Usage:
```
    ballerina ballerina-file [args...]
    ballerina command
```

**Available Commands:**

    version     Print Ballerina version.
    help        Print help message.

####Examples:

1) Execute the main function of the `say-hello-world.bal` Ballerina program with no arguments.
```
    ballerina say-hello-world.bal
```

2) Execute the main function of the `hello-world.bal` Ballerina program with argument `WSO2`.
```
    ballerina hello-world.bal WSO2
```
3) Execute the main function of the `hello-world.bal` Ballerina program with argument `WSO2 Inc`. If the argument value contains a space, use double quote to enclose it
```
    ballerina hello-world.bal "WSO2 Inc" 
```
4) Execute the main function of the `multiplier.bal` Ballerina program with arguments `5` and `10`.
```
    ballerina multiplier.bal 5 10
```
5) Print Ballerina version. 
```
    ballerina version
```
6) Print `ballerina` help message. 
```
    ballerina help
```

### **Server Mode** 

Start Ballerina as a server. The Ballerina runtime will deploy all Ballerina services in given Ballerina programs,
and start accepting requests for deployed services. 

####Usage:
```
    ballerinaserver [option] ballerina-file1 ballerina-file2 ...
    ballerinaserver command
```

**Available Options (Unix/Linux Only)**

    start       Start Ballerina Server in background.
    stop        Stop Ballerina Server which is running in the background.
    restart     Restart Ballerina Server which is running in the background.

**Available Commands**

    version     Print Ballerina version.
    help        Print this help message.
    
####Examples:

1) Deploy `passthrough.bal` Ballerina program. 
```
    ballerinaserver passthrough.bal
```
2) Deploy `passthrough.bal` and `echo.bal` Ballerina programs. 
```
    ballerinaserver passthrough.bal echo.bal
```
3) Print Ballerina version.
```
    ballerinaserver version
```
4) Print `ballerinaserver` help message.
```
    ballerinaserver help
```

## Working with Ballerina Editor. 

Start Ballerina editor using,

```
editor
```

Access Ballerina editor via [http://localhost:9091](http://localhost:9091)


