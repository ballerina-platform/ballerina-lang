# Ballerina Format CLI Tool

The Ballerina format CLI tool can be used to format Ballerina sources
according to the default [Ballerina style guide](https://github.com/ballerina-platform/ballerina-lang/tree/master/docs/style_guide).

## Using the Ballerina Format CLI Tool

The Ballerina format command can be used to format Ballerina source files.

```sh
$ ballerina format --help
Prints the help guide for the Ballerina format tool.

Usage:
ballerina format [<ballerinaFile> | <moduleName>] [-d | --dry-run]
  ballerinaFile:
      Path of a single Ballerina source file, which needs to be formatted.
  moduleName:
      The name of the Ballerina module, which has Ballerina source files that need to
      be formatted. The format command should be executed from the root of the Ballerina project.
Flags:
  -d
  --dry-run 
      By providing this option, you can dry run the formatter and see which files will
      be formatted after the execution.
```

### Usage Examples

**Example 1:** Formats all the Ballerina source files in a Ballerina project.
```sh
$ ballerina format
```

This command should be executed from the root of the Ballerina project.

**Example 2:** Formats all the Ballerina source files in a Ballerina module.
```sh
$ ballerina format module1
```
This command should be executed from the root of the Ballerina project.

**Example 3:** Formats a single Ballerina source file.
```sh
$ ballerina format hello.bal
```

**Example 4:** Performs a dry run of the formatter to see which files will be formatted
if executed.
```sh
$ ballerina format -d
$ ballerina format module1 -d
$ ballerina format hello.bal -d
```
