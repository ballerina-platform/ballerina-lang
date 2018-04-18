## Package Overview
This package provides functions to retrieve Operating System (OS) related metadata and environmental variables. Environment variables are dynamic key value pairs that influences the behaviour of the processes running on an OS, such as `BALLERINA_HOME` and `PATH`. 
### Retrieve OS metadata 
The `getArchitecture()`, `getVersion()`, and `getName()` functions return the architecture, version, and name of the OS where the program is executed. 
### Retrieve environment variables
The environmental variables can have many values and these value can be separated using  path separators. The Path separator separates the path values and it is OS specific. Unix-based systems use the colon `:` as the path separator while Windows-based systems use the semicolon `;` as the path separator.

The table given below shows how the `getEnv()` and `getMultivaluedEnv()`functions return values that are separated by path separators.

Statement |Return Values
--- | --- 
`os:getEnv("PATH");` | `“/home/user/ballerina/test:/home/user/ballerina/examples”`
`os:getMultivaluedEnv("PATH");` |`[“/home/user/ballerina/test”, “/home/user/ballerina/examples”]`

The `getEnv()` function returns the value of the environment variable in the same format as it stored. If the `getEnv()` function calls an `environmentVariable` that does not exist, it returns `null`.

If the environment variable returns a value that has more than one path and they are separated by path separators, the `getMultivaluedEnv()` function returns these values as an array of paths. 

## Samples
### Retrieve OS metadata
The sample given below prints the name, version, and architecture of the OS.

```ballerina
function main(string... args) {
   io:println("OS meta-data");
   // Prints the name of the OS.
   io:println("Name : " + os:getName());
   // Prints the version of the OS.
   io:println("Version : " + os:getVersion());
   // Prints the architecture of the OS.
   io:println("Architecture :" + os:getArchitecture());
}
```
### Retrieve environment variables
The function given below returns the value of the environment variable.

```ballerina
function main(string... s) {
   string environmentVariable = "BALLERINA_HOME";
   // Get the environment variable value.
   string environmentValue = os:getEnv(environmentVariable);
   // Print the value of the environment variable.
   io:println("Value of environment variable '" + environmentVariable + "': " + environmentValue);
}
```
