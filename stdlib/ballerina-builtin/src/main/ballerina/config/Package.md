## Package overview

The `ballerina/config` package provides the Config API to read configurations from environment variables, files in the TOML format, and command-line parameters and build a consolidated set of configurations. 

The precedence order for configuration lookup is as follows: 
1. CLI parameters (used with the -e flag)
2. Environment variables 
3. Configuration files in the TOML format

If a configuration is defined in both a configuration file and as an environment variable, the environment variable takes precedence. Similarly, if the same is set as a CLI parameter, it replaces the value of the environment variable. This configuration resolution happens at the start of the program execution. Configurations can be set programmatically as well. 

The Config API provides the capability to feed sensitive data (e.g., passwords) to Ballerina programs securely, by encrypting them. 


## Samples

### Setting configurations

To explicitly specify a configuration file, the `--config` or `-c` flag can be used. If this flag is not set, when running a project, Ballerina looks for a `ballerina.conf` file in project root. When running a single file or a balx, it's picked from the same dir as balx or the source. The path to the configuration file can either be an absolute or a relative path. 

```sh
ballerina run my-program.bal --config /path/to/conf/file/custom-config-file-name.conf
```

A configuration file should conform to the TOML format. Ballerina only supports the following features of TOML: value types (string, int, float and boolean), tables and nested tables. 
Given below is a sample:

```
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

```
ballerina run my-program.bal -e b7a.http.tracelog.console=true -e b7a.http.tracelog.path=./trace.log -e b7a.http.accesslog.console=true -e b7a.http.accesslog.path=./access.log
```

Configurations in a file can be overridden by environment variables. To override a particular configuration, an environment variable that matches the configuration key must be set. As periods are not allowed in environment variables, periods in a configuration key should be replaced by underscores.

```
// In Linux and Mac.
$ export b7a_http_tracelog_path=”./trace.log”
$ export b7a_http_accesslog_path=”./access.log”

// In Windows.
$ set(x) b7a_http_tracelog_path=”./trace.log”
$ set(x) b7a_http_accesslog_path=”./access.log”
```

If the configurations need to be shared during runtime, they can be set using the `setConfig()` function. Such configs, too, are accessible to the entire BVM (Ballerina Virtual Machine). 

```ballerina
config:setConfig("john.country", "USA");
```
 
### Reading configurations

The API provides functions to read configs in their original type.

```ballerina
// Reads a configuration as a string.
string host = config:getAsString("host"); // Returns “” (i.e., empty string) if the configuration is not available.

// Reads a configuration as an integer.
int port = config:getAsInt("port"); // Returns 0 if the configuration is not available.


// Reads a configuration as a float.
float rate = config:getAsFloat("rate"); // Returns 0.0 if the configuration is not available.

// Reads a configuration as a boolean.
boolean enabled = config:getAsBoolean("service.enabled"); // Returns ‘false’ if the configuration is not available.
```
When reading a configuration, a default value can be specified as well. If a default value is specified, it is returned if a configuration entry cannot be found for the specified key.

```ballerina
// Reads a configuration as a string, and if it does not exist, returns “localhost”.
string host  = config:getAsString("host", default = "localhost"); 
```

The `contains()` function is used to check whether a configuration entry exists for the specified key. 

```ballerina
// Checks whether the configuration is available.
boolean configAvailable = config:contains("host"); 
```

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
map serverAlphaMap  = config:getAsMap("b7a.http.tracelog"); // here, the map’s key-value pairs represent config key-value pairs
```

In the above configuration file, the `host` is specified as `@env:{TRACE_LOG_READER_HOST}`. When resolving the configurations, Ballerina looks for a variable named `TRACE_LOG_READER_HOST` in environment variables and maps `b7a.http.tracelog.host` to its value. 

### Securing configuration values

Sensitives values can be encrypted using the `encrypt` command as follows:

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

```
[admin]
password=”@encrypted:{JqlfWNWKM6gYiaGnS0Hse1J9F/v48gUR0Kxfa5gwjcM=}”
```
### Reading config files with encrypted values

When trying to run a Ballerina program with a configuration file that contains encrypted values, the user is prompted to enter the secret, which was used to encrypt the values. Values are decrypted only on demand, when an encrypted value is looked up using the `getAsString()` function.

```
$ ballerina run program.bal 
ballerina: enter secret for config value decryption:
```

**Note**: *The same config file cannot contain values that are encrypted using different secrets.* 

## Package contents
`<auto-generated from the code comments>`
