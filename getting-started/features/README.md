# Features

## Running Ballerina programs 

You can run Ballerina main/service programs with only few clicks. You don't have to add or change any configurations.

![alt text](images/RunConfig.gif)

## Debugging Ballerina programs 

You can debug Ballerina main/service programs with only few clicks as well.

![alt text](images/DebugSupport.gif)

## Import packages on the fly

This feature adds import declarations on the fly. All you need to do is selecting the package name from the lookup list and the package declaration will be added automatically.

![alt text](images/AutoImports.gif)

## Auto import unambiguous packages

When we copy and paste some Ballerina codes, this feature will automatically import unambiguous imports. Then we can manually import any packages with ambiguity using clicking on the package name and pressing `Alt + Enter` keys.

**Note:** Please note that this feature is disabled by default since this might cause issues if the file contain grammar mistakes. You can enable it by selecting the **Add unambiguous imports on the fly** CheckBox in **Settings -> Languages and Frameworks -> Ballerina -> Auto Imports**.

![alt text](images/AutoImports2.gif)

## Parameter Infomation

You can view the required parameters of a functions, actions, etc by pressing `Ctrl`+`P`.

![alt text](images/ParameterInfo.gif)

## Quick Documentation

You can view the documentation of a functions, actions, etc by pressing `Ctrl`+`Q`.

![alt text](images/QuickDocumentation.gif)

## Struct fields suggestions

Struct fields are suggested inside struct initializing braces and after the dot operator. Multi level struct field access is available.

![alt text](images/StructFields.gif)

## Annotation fields suggestions

Annotation field names will be suggested inside annotation attachments.

![alt text](images/AnnotationFields.gif)

## Array length field suggestion

Array length property is now available. This is an array dimension aware suggestion.

![alt text](images/ArrayLength.gif)

## Find Usage

You can find usage of variables, functions, structs, connectors, etc.

![alt text](images/FindUsage.gif)

## Go To Definition

You can go to definition of variables, function invocations, etc by `Ctrl`+`Click` on the reference.

![alt text](images/GoToDefinition.gif)

## Formatting

You can reformat the Ballerina codes by pressing `Ctrl`+`Alt`+`L`.

![alt text](images/Formatting.gif)

## Path/Query Parameter validation

Path/Query parameter validation is available to improve usability.

![alt text](images/PathParameter.gif)

## Package Inspection

Package inspection is available to improve usability. This will suggest to add the package name if no package name is present. If an incorrect package name is present, it will suggest to change the package name. You can view these suggestions using `Alt`+`Enter`.

![alt text](images/PackageFix.gif)

## File Templates

Three types of Ballerina file templates are available.
1) Ballerina Main - Contains a sample main program
2) Ballerina Service - Contains a sample service
3) Empty File

![alt text](images/FileTemplates.gif)

## Live Templates

Live templates contains boilerplate codes and lets the user to enter them easily. All available live templates can be viewed/changed at **Settings -> Editor -> Live Templates -> Ballerina**.

![alt text](images/LiveTemplates.gif)

## Hidden Templates

Hidden templates are associated with most of the keywords. Hidden templates can be invoked by pressing `Ctrl`+`Space` after typing the keyword. Hidden template will be inserted when you select the keyword from the lookup element list as well.

![alt text](images/HiddenTemplates.gif)

## Spell Checking

Spell checking is enabled for all identifiers. You can rename all of the definitions and references as well.

![alt text](images/SpellChecking.gif)
