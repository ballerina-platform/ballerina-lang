# Running a Ballerina Program
This page describes how to run a Ballerina program, either in standalone or server mode, in [packaged](lang-ref/packaging.md) or unpackaged format.

## Standalone mode

The following command is used to execute the `main()` function of a Ballerina program from a `.bal` file, package, or archive file:

```
ballerina run main (filename | packagename | mainarchive)
```

#### Examples
1) Execute the `main()` function of the `say-hello-world.bal` Ballerina program with no arguments.
```
ballerina run main say-hello-world.bal
```
2) Execute the `main()` function of the `hello-world.bal` Ballerina program with argument `WSO2`.
```
ballerina run main hello-world.bal WSO2
```
3) Execute the `main()` function of the `hello-world.bal` Ballerina program with argument `WSO2 Inc`. If the argument value contains a space, use double quotes to enclose it.
```
ballerina run main hello-world.bal "WSO2 Inc" 
```
4) Execute the `main()` function of the `multiplier.bal` Ballerina program with arguments `5` and `10`.
```
ballerina run main multiplier.bal 5 10
```

## Server mode

The following commands are used to run Ballerina programs as services:

To run named services:

```
ballerina run service (filename | packagename | servicearchive)+ 
```

To run a collection of service archives from service root:

```
ballerina run service [-sr serviceroot]
```

If a Docker image is built at build time, the execution of that image is done using normal Docker commands. For details, see Ballerina Docker Architecture.

TODO: add link to that doc once it's checked in.

## Additional commands

To print the Ballerina version: 
```
ballerina version
```

To print help on using the Ballerina commands:
```
ballerina help
```
