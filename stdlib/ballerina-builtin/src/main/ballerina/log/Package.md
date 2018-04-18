## Package Overview

This package provides a basic API to manage logs in packages and files. 

### Loggers 

Loggers are defined over packages and there are dedicated loggers for each package. Packages that have loggers defined over it prints the logs that include the date, time, log level, package name, and more.  For example, given below is a log printed for the `foo` package.

```2018-04-09 11:33:21,300 ERROR [foo] - This is an error log.```

### Log Outputs 

In Ballerina, all the logs are written to the `stderr` stream by default. Therefore, the logs are printed in the console. This makes it easy to debug the code in a container environment.

Log outputs can be printed to a log file using a CLI argument or using configurations.

Different output logging levels can be defined for packages. For more information, see the **Log Level** section.

### Log Level

This package provides functions to log at the `DEBUG`, `ERROR`, `INFO`, `TRACE`, `WARN`, `OFF`, and `ALL` levels. By default, all log messages are logged to the console at the `INFO` level. The `OFF` log level turns off logging and the `ALL` log level allows all log levels.

## Sample  

Follow the steps given below to run the sample and get sample log outputs.

1. Create a directory named `foo`, add the code given below to a  file, and name the file `test.bal`.


```ballerina
	package foo;
	import ballerina/log;

	function main(string[] args) {
    	  error err = {message: "error occurred"};
    	  log:printDebug("debug log");
    	  log:printError("error log");
    	  log:printErrorCause("error log with cause", err);
    	  log:printInfo("info log");
    	  log:printTrace("trace log");
    	  log:printWarn("warn log");		
	}
```

2. Navigate to the directory where the `test.bal` file is saved via the terminal and run the file using the command given below.

      `ballerina run foo/`

3. By default, the logs are printed to the console. To write logs to the `test.log` file, execute the following command.

     `ballerina run foo/ 2> test.log`


4. Controlling the log level of the `foo` package:

* The log level for the `foo` package can be changed to ERROR as follows using CLI arguments.

     `ballerina run foo/ -e foo.loglevel=ERROR`


* To set the log level of `foo` pacakge using a configuration file, create a file named `ballerina.conf` in the source root of `test.bal`, and place the following entry..

     ```ballerina
      [foo]
      loglevel="ERROR"
     ```
   Next, run the `test.bal` file to print the ERROR logs.
    `ballerina run foo/`
