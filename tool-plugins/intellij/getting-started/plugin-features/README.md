# Plugin Features

The following features are supported by the plugin, as listed below.

- [Running Ballerina Programs](#running-ballerina-programs)
- [Debugging Ballerina Programs](#debugging-ballerina-programs) 
- [Import modules On-the-fly](#import-modules-on-the-fly)
- [Auto Import Unambiguous Modules](#auto-import-unambiguous-modules)
- [Find Usage](#find-usage)
- [Go to Definition](#go-to-definition)
- [Formatting](#formatting)
- [Parameter Information](#parameter-information)
- [Quick Documentation](#quick-documentation)
- [Object/Record Fields suggestions](#objectrecord-fields-suggestions)
- [Annotation fields suggestions](#annotation-fields-suggestions)
- [File Templates](#file-templates)
- [Live Templates and Code snippets](#live-templates-and-code-snippets)
- [Spell Checking](#spell-checking)
- [Semantic Analyzing](#semantic-analyzing) 

## Running Ballerina Programs 

You can run Ballerina main/service programs with only few clicks. You don't have to add or change any configurations.

![alt text](images/RunConfig.gif)

## Debugging Ballerina Programs 

You can debug Ballerina main/service programs with only few clicks as well.

![alt text](images/DebugSupport.gif)

## Import Modules On-the-fly

This feature adds import declarations on the fly. All you need to do is selecting the module name from the lookup list and the module declaration will be added automatically.

![alt text](images/AutoImports.gif)

## Auto Import Unambiguous Modules

When you copy and paste some Ballerina code, this feature automatically imports unambiguous imports. Then you can manually import any modules with ambiguity by clicking on the module name and pressing `Alt + Enter` keys.

**Note:** Please note that this feature is disabled by default since this might cause issues if the file contains 
grammar mistakes. 
You can enable it by selecting the **Add unambiguous imports on the fly** CheckBox in **Settings -> Languages and Frameworks -> Ballerina -> Auto Imports**.

![alt text](images/AutoImports2.gif)

## Find Usage

You can find usage of variables, functions, etc.

![alt text](images/FindUsage.gif)

## Go to Definition

You can go to definition of variables, function invocations, etc by `Ctrl`+`Click` on the reference.

![alt text](images/GoToDefinition.gif)

## Formatting

You can reformat the Ballerina codes by pressing `Ctrl`+`Alt`+`L`.

![alt text](images/Formatting.gif)

## Parameter Information

You can view the required parameters of a functions, remote functions, etc. by pressing `Ctrl`+`P`.

![alt text](images/ParameterInfo.gif)

## Quick Documentation

You can view the documentation of a functions, remote functions, etc by pressing `Ctrl`+`Q` or hovering over the element while pressing `Ctrl`. 

![alt text](images/QuickDocumentation.gif)

## Object/Record Fields Suggestions

Fields are suggested inside the objects/records after initializing braces and the dot operator.

![alt text](images/StructFields.gif)

## Annotation Fields Suggestions

Annotation field names are suggested inside annotation attachments.

![alt text](images/AnnotationFields.gif)

## File Templates

Three types of Ballerina file templates are available.
1) Ballerina Main - Contains a sample main program
2) Ballerina Service - Contains a sample service
3) Empty File

![alt text](images/FileTemplates.gif)

## Live Templates and Code Snippets

Live templates/code snippets contain boilerplate codes and allows you to enter them easily. All available live templates can be viewed/changed at **Settings -> Editor -> Live Templates -> Ballerina**.

![alt text](images/LiveTemplates.gif)

## Spell Checking

Spell checking is enabled for all identifiers. You can rename all of the definitions and references as well.

![alt text](images/SpellChecking.gif)

## Semantic Analyzing

Ballerina IDEA plugin provides semantic analyzing and diagnostics capabilities through Ballerina Language Server.  
**Note:** This feature is disabled by default due to performance related reasons. You can enable this in `Settings ->
 Languages & Frameworks -> Ballerina -> Semantic Analyzer`.

![alt text](images/SemanticAnalyzer.png)

