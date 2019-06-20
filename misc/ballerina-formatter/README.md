# Ballerina Format CLI Tool

Ballerina format CLI tool is the tool that can be used to format the ballerina sources
according to the default [Ballerina style guide](https://github.com/ballerina-platform/ballerina-lang/tree/master/docs/style_guide)

## Using Ballerina Format CLI tool

Ballerina format command can be used to format ballerina source files.

```sh
$ ballerina format --help
Get format tool help page.

Usage:
ballerina format [<ballerinaFile> | <moduleName>] [-d | --dry-run]
  ballerinaFile:
      Paths to a single ballerina source file which needs to be formatted.
  moduleName:
      Name of the module which has ballerina source files which need to
      be formatted. Command should be executed from the ballerina project root.
Flags:
  -d
  --dry-run 
      by providing this option user can dry run formatter and see which files will
      be formatted after execution.
```

### Usage Examples

**Example 1:** Format all the Ballerina source files in a Ballerina project.
```sh
$ ballerina format
```

This command should be executed from the Ballerina project root.

**Example 2:** Format all the Ballerina source files in a Ballerina module.
```sh
$ ballerina format module1
```
This command should be executed from the Ballerina project root.

**Example 3:** Format a single Ballerina source file.
```sh
$ ballerina format hello.bal
```

**Example 4:** Dry run the formatter to see which files will be formatted
if executed.
```sh
$ ballerina format -d
$ ballerina format module1 -d
$ ballerina format hello.bal -d
```
