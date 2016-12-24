# Working with Ballerina Runtime. 

Ballerina provides two runtime modes. 

### **Standalone Ballerina runtime** 

This runtime allows to execute either a Ballerina `main` function or start Ballerina `services` defined in a given Ballerina file. 

If given Ballerina file contains the `main` function, Ballerina runtime will execute it with given arguments. These arguments are optional. Once `main` function completes its execution, Ballerina runtime will exit. 

If the Ballerina file contains Ballerina Services, The Ballerina runtime will deploy all Ballerina services in given file and start accepting requests for deployed services. 

If the Ballerina file contains both `main` and Ballerina `services`, then the main function takes precedence. The Ballerina runtime execute only the `main` function then exit.
 
If the Ballerina file does not contain a `main` function or any Ballerina `Service`, Runtime will exit with an Error.  

### **Ballerina Server runtime** 

Start Ballerina runtime as a server. In this mode, the current directory will consider as deployment directory, unless it is overridden by `-servicespath` option. When Ballerina runtime starts, it deploys all Ballerina file, Ballerina Applications and archives (zip) in current deployment directory and its sub-directories.  

Only Ballerina services will get deployed and No `main` functions will not run in this mode. 

### Usage 

**Running Ballerina in Standalone Mode.** 
```
ballerina.sh service.bal [command] [-options]
```

Executes either the Ballerina main function or start Ballerina services defined in the 'service.bal' Ballerina file.

**Running Ballerina in Server Mode.**

```
ballerinaserver.sh [command] [-options]
```
Starts Ballerina Server in current terminal.

**Supported commands**

| Command | Description |
|---|---|
**start**  | Start Ballerina runtime in background.| 
**stop**  | Stop Ballerina runtime running in background. |
**restart** | Restart Ballerina runtime in background. | 
**version** | Print Ballerina version. |  
**help**  | Print help message. | 

Note: If multiple Commands are defined as arguments last command will take precedence.

**Supported -options**

| Options | Description | Supported Scripts |
|---|---|---|
|**-bargs** values... | Defines input arguments for Ballerina main function. Values are separated by space. Default value is _empty_ | ballerina.sh |
|**-servicespath** path <br/>or<br/> -s path | Overrides default servicespath '.' with 'path'. | ballerinaserver.sh |
|**-bpath** location | Set Ballerina Path to `localtion`. | ballerina.sh & ballerinaserver.sh |

