# Ballerina Format CLI Tool

Ballerina format CLI tool is the tool that can be used to format the ballerina sources
according to the default [Ballerina style guide](https://github.com/ballerina-platform/ballerina-lang/tree/master/docs/style_guide)

## Using Ballerina Format CLI tool

Ballerina format command can be used to format ballerina source files.

```sh
$ ballerina format --help
Get format tool help page.

Usage:
ballerina format [<ballerinaFile>|<moduleName>] [-ow|--over-write]
  ballerinaFile:
      Paths to a single ballerina source file which needs to be formatted.
  moduleName:
      Name of the module which has ballerina source files which need to
      be formatted. Command should be executed from the ballerina project root.
Flags:
  -ow
  --over-write  
      Overwrite the Ballerina source file content with formatted source
      without prompting for user confirmation.
      
      CAUTION: After applying this option tool will forcefully update original source
      file with formatted source and the this process is not reversible.
```

### Usage Examples

**Example 1:** Format all the Ballerina source files in a Ballerina project.
```sh
$ ballerina format
```

This command should be executed from the Ballerina project root and
This will prompt for user confirmation before replacing file contents.

**Example 2:** Format all the Ballerina source files in a Ballerina module.
```sh
$ ballerina format module1
```
This command should be executed from the Ballerina project root and
This will prompt for user confirmation before replacing file contents.

**Example 3:** Format a single Ballerina source file.
```sh
$ ballerina format hello.bal
```
This will prompt for user confirmation before replacing the file content.

**Example 4:** Execute the command with the consent for overwriting the original source with
formatted source.
```sh
$ ballerina format -ow
$ ballerina format module1 -ow
$ ballerina format hello.bal -ow
```

Using `-ow` option will override the prompt for confirmation and continue to replace the original source with
formatted source.
