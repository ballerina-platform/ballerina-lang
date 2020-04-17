## Module Overview

This module provides the Config API to read configurations from environment variables, TOML files, and command-line parameters and build a consolidated set of configurations.

The precedence order for configuration lookup is as follows: 
1. CLI arguments (used with the --)
2. Environment variables 
3. Configuration files in the TOML format

This configuration resolution happens at the start of the program execution. Configurations can be set programmatically as well. 

The Config API provides the capability to feed sensitive data (e.g., passwords) to Ballerina programs securely by encrypting them. 

### Setting configurations

To specify a configuration file explicitly, the `--b7a.config.file=<path to configuration file>` property can be used. If 
this property is not set when 
running a project, Ballerina looks for a `ballerina.conf` file in the project root. When running a single file or a
 `.jar`, it's picked from the same directory in which the `.jar` or source resides. The path to the configuration
  file can either be an absolute or a relative path. 

```sh
ballerina run my-program.bal --b7a.config.file=/path/to/conf/file/custom-config-file-name.conf 
```

A configuration file should conform to the TOML format. Ballerina only supports the following features of TOML: value types (string, int, float and boolean), tables, and nested tables. 
Given below is a sample:

```toml
[b7a.http.tracelog]
console=true
path="./trace.log"

[b7a.http.accesslog]
console=true
path="./access.log"
```
A key given inside a bracket forms a namespace. Any configuration that is specified between two such keys belongs to the namespace of the first of these keys. To access a configuration in a namespace, the fully qualified key should be given (e.g., `b7a.http.tracelog.path`).

The following types can be given through a configuration file: `string`, `int`, `float`, and `boolean`. If the configuration value is not an `int`, `float`, or a `boolean`, it is considered a `string` and should always be quoted.

The same configs can be set using CLI parameters as follows.

```bash
ballerina run my-program.bal --b7a.http.tracelog.console=true --b7a.http.tracelog.path=./trace.log
  --b7a.http.accesslog.console=true --b7a.http.accesslog.path=./access.log
```

Configurations in a file can be overridden by environment variables. To override a particular configuration, an environment variable that matches the configuration key must be set. As periods are not allowed in environment variables, periods in a configuration key should be replaced by underscores.

```bash
// In Linux and Mac.
$ export b7a_http_tracelog_path=”./trace.log”
$ export b7a_http_accesslog_path=”./access.log”

// In Windows.
$ set(x) b7a_http_tracelog_path=”./trace.log”
$ set(x) b7a_http_accesslog_path=”./access.log”
```

If the configurations need to be shared during runtime, they can be set using the `setConfig()` function. 

```ballerina
config:setConfig("john.country", "USA");
```
 
### Reading configurations

The API provides functions to read configurations in their original type. Check function descriptors for example usages.

A set of configurations belonging to a particular namespace can be retrieved as a `map` using the `getAsMap()` function. Here is an example:

```toml
[b7a.http.tracelog]
console=true
path="./trace.log"
host="@env:{TRACE_LOG_READER_HOST}"
port=5757

[b7a.http.accesslog]
console=true
path="./access.log"
```

The configurations for HTTP trace logs can be retrieved as a `map` as follows:

```ballerina
// Reads a configuration section as a map.
// Here, the map’s key-value pairs represent config key-value pairs.
map<anydata> serverAlphaMap  = config:getAsMap("b7a.http.tracelog");
```

In the above configuration file, the `host` is specified as `@env:{TRACE_LOG_READER_HOST}`. When resolving the configurations, Ballerina looks for a variable named `TRACE_LOG_READER_HOST` in the environment variables and maps `b7a.http.tracelog.host` to its value.

If the specified environment variable does not exist, it will will treat `@env:{TRACE_LOG_READER_HOST}` as a normal string value.

### Securing configuration values

Sensitive values can be encrypted using the `encrypt` command as follows:

```sh
$ ballerina encrypt
Enter value: 

Enter secret: 

Re-enter secret to verify: 

Add the following to the runtime config:
@encrypted:{JqlfWNWKM6gYiaGnS0Hse1J9F/v48gUR0Kxfa5gwjcM=}

Or add the following to the runtime command-line:
-e<param>=@encrypted:{JqlfWNWKM6gYiaGnS0Hse1J9F/v48gUR0Kxfa5gwjcM=}
```
This encrypted value can then be placed in a configuration file or provided as a CLI parameter.

```bash
[admin]
password=”@encrypted:{JqlfWNWKM6gYiaGnS0Hse1J9F/v48gUR0Kxfa5gwjcM=}”
```
### Reading config files with encrypted values

When trying to run a Ballerina program with a configuration file or CLI parameters that contain encrypted values, Ballerina will first check to see if the `b7a.config.secret` configuration is set. This configuration is used to set the path to a file containing the secret required to decrypt the configurations. If it is set, the secret is read, and the secret file is deleted.

If a secret file is not provided, the user is prompted to enter the secret. Values are decrypted only on demand when an encrypted value is looked up using the `getAsString()` function.

```bash
$ ballerina run program.bal 
ballerina: enter secret for config value decryption:
```

**Note**: *The same config file cannot contain values that are encrypted using different secrets.*
