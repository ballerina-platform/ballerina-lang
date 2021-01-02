# Ballerina Shell Design Document

## Invoker

![Preprocessor](../docs/sequence.png)

Currently, the approach is as follows. The overview sequence diagram is shown above as well.

- The application front-end will supply the correctly categorized snippet. The snippet type refers to the location that the code segment should go to. For example, `ImportSnippets` will go to the topmost section. Refer [HERE](#front-end) to view the front-end implementation details.

- The invoker will then generate the `.bal` file using the snippet data, the previously defined module-level declarations, and variable details. The snippet will be generated using the template [HERE](#invoker-template). The variable type information required to generate the file is derived via the `PackageCompilation`. To enable imports, only the used imports are included. (Because unused imports are not allowed)

- The invoker will then execute the generated ballerina file. A custom security manager is employed to stop VM from exiting because of `System.exit()` calls in the ballerina executable. 

- In the execution, the ballerina file will load the variable values from the memory class. This memory class is a class with a static map that exposes static methods: `recall` and `memorize`. 

- - `memorize` will save the variable value as a plain Java Object. `recall` will return the saved object. Since the life-time of this class is persistent throughout every execution phase, the variable values saved will be available for each execution. (So this will act as persistent storage for the ballerina global variables)
  - At the start of each execution, all variables are loaded from the Java memory class and cast into the required type. Then the snippet is executed and all the variables are saved back in the memory class.

- Additionally, a context id is used to allow several invoker sessions to run. If there are any errors, they are reported as necessary.

## Front End

### Preprocessor

The preprocessor is the first transformational phase of the program. Any input is sent through the preprocessor to convert the input into a list of individually processable statements. For example, any multiple statement input will be split into the relevant list of string counterpart at the end of this phase. The implemented `SeparatorPreprocessor` currently splits the statements into separated lists depending on the semicolons that are in the root bracket level. The motivation of a preprocessor is to divide the input into separately identifiable sections so each can be individually processed. Removing comments is also a task done by the preprocessor.

![Preprocessor](../docs/preprocessor.png)

Currently, following preprocessors are implemented.

| Preprocessor Name      | Description                                                  |
| ---------------------- | ------------------------------------------------------------ |
| Separator preprocessor | Preprocessor to split the input into several statements based on the semicolons and brackets and remove the comments. |

Following are some inputs and expected output of the preprocessor for reference.

| Input                                             | Expected Output                                     |
| ------------------------------------------------- | --------------------------------------------------- |
| `int number`                                      | [`int number`]                                      |
| `int number; number = 100;`                       | [`int number;`, `number = 100;`]                    |
| `function () { int p = 100; string h = "hello";}` | [`function () { int p = 100; string h = "hello";}`] |
| `int a = 0; while (a < 10) { a+= 1; }`            | [`int a = 0;`, `while (a < 10) { a+= 1; }`]         |

### Tree Parser

In this stage, the correct syntax tree is identified. The root node of the syntax tree must be the corresponding type for the statement. For example, for an import declaration, the tree that is parsed should have `ImportDeclarationNode` as the root node.

![Preprocessor](../docs/treeparser.png)

Currently, following tree parsers are implemented.

| Tree Parser Name  | Description                                                  |
| ----------------- | ------------------------------------------------------------ |
| Trial Tree Parser | Parses the source code line using a trial based method. The source code is placed in several places and is attempted to parse. This continues until the correct type can be determined. |

> TODO: Find a way to create a tree parser that directly uses ballerina parser without depending on trials. Trial parsing is too slow.

Following are some inputs and expected output of the tree parser for reference.

| Input                       | Expected Output Root Node       |
| --------------------------- | ------------------------------- |
| `import ballerina/io;`      | `ImportDeclarationNode`         |
| `int variable = 100;`       | `ModuleVariableDeclarationNode` |
| `while (a) { int i = 100;}` | `WhileStatementNode`            |

### Snippet Factory

Snippets are individual statements.

Every snippet must have a **kind** (which dictates where the snippet should go). Each snippet must refer to a single statement. That means if the same input line contained several statements, it would be parsed into several snippets. (This separation is done in the preprocessor.)

In processing the snippets, if a snippet contained an error and failed to run, the execution of the snippet would be stopped. If the snippet was contained in a line with more snippets, (if the input contained multiple snippets) all the snippets would be ditched. This also means that an error snippet is taken as if it were never given. 

Also, names given to the REPL may never be overridden. (If x variable is defined, you cannot redefine variable x even with the same type. The same goes for functions, classes, etc..) However, any valid redeclaration in a different scope may be possible.

#### Snippet Base Type

Snippets are defined in terms of the source code it contains.

![Preprocessor](../docs/snippet.png)

Snippets would be of mainly 5 categories. Erroneous snippets are rejected as soon as they are detected. So, there isn't a category for them.

#### Import Snippet

Snippets that represent an import statement.

#### Variable Declaration Snippet

In the REPL, only module-level variables are allowed. The main motivation of that is to keep a global state. (So every defined variable is available to others.) Thus, `VariableDeclarationNodes` will also be converted into `ModuleVariableDeclarationNode`. However, the ability of variable declarations to not have an initializer will be an issue. Currently, the declaration will be converted as is. If there isn't a initializer, compiler will give an error.

> Infer a filler value if possible (TODO?)

Because every declaration is a module-level declaration, it must have an initializer. However, for usability, some variables should have a default value to initialize if the initializer is not given. For example, an integer can be initialized with a 0 if an initializer is not provided. However, this will only be done for a selected few types where a default initializer is trivial.

Following initializers will be used when an initializer is not provided. Note that, because of lack of information at the stage of this operations, it is not possible to infer the default types of the types which are defined by the user. This will also include var, which is a type determined by the compiler. The table also includes whether the type is serializable.

| Type                             | Filler Default Value                          |
| -------------------------------- | --------------------------------------------- |
| `()`                             | `()`                                          |
| `boolean`                        | `false`                                       |
| `int`, `float`, `double`, `byte` | `0`                                           |
| `string`                         | `""`                                          |
| `xml`                            | `xml '<!---->'`                               |
| `array`, `tuple`                 | `[]`                                          |
| `map`                            | `{}`                                          |
| `record`                         | `{}`                                          |
| `table`                          | `table []`                                    |
| `any`                            | `()`                                          |
| `union`                          | Any available filler value of types in union. |
| `optional`                       | `()`                                          |
| `json`, `anydata`                | `()`                                          |
| Any other type                   | No filler value                               |

*Default values for xml, array, tuple, map, record have the potential to fail or cause errors. However, they are still used if the user didn't provide one in the expectation that the user will identify the error if the default value failed. [Ballerina Default Fill Members](https://ballerina.io/spec/lang/2020R1/#FillMember)

Following are some inputs and expected filler values for reference.

##### Issues with Data types

- **Inherently Fully Supported** - `nil`, `boolean`, `int`, `float`, `decimal`, `string`, `xml`, `array`, `tuple`, `map`, `record`, `table`, `error`, ..., `singleton`, `union`, `optional`, `any`, `anydata`, `never`, `byte`, `json`,  `var`
- **Not fully supported** - `function`, `object`
  - Global variables is functions will same value as they were when the function is defined. (This issue is not applicable for normal function definitions, just anonymous function definitions) So anonymous function declarations should be disabled. (Or show a warning) - Use normal function declarations instead. Objects are not supported because of similar reason as functions.

##### Things that you should not do

- Define variables of function type or object where the function/object depends on a global variable.

#### Module Member Declaration Snippet

Module-level declarations. These are not active or runnable. Service declaration can start a service on a port, etc... All other declarations are just declarations. They do not execute to return a value. Documentations and public keyword cannot be used in the module level declarations.

| Sub Kind Name                    | State | Notes                                                        |
| -------------------------------- | ----- | ------------------------------------------------------------ |
| Function Definition              | OK    |                                                              |
| Listener Declaration             | OK    | There must be a initializer. However, there may be undefined variables inside the initializer. |
| Type Definition                  | OK    |                                                              |
| Service Declaration              | ERROR | Has a side effect of starting a server.                      |
| Constant Declaration             | OK    | Constant variables are always defined in the module level.   |
| Module Variable Declaration      | MOVED | Moved responsibility into Variable Declaration Kind.         |
| Annotation Declaration           | OK    | ?                                                            |
| Module XML Namespace Declaration | OK    |                                                              |
| Enum Declaration                 | OK    |                                                              |
| Class Definition                 | OK    |                                                              |

#### Statement Kind

These are normal statements that should be evaluated from top to bottom inside a function. Fail Statement Sub Kind is not accepted. 

| Sub Kind Name                       | State | Notes                                                        |
| ----------------------------------- | ----- | ------------------------------------------------------------ |
| Assignment Statement                | OK    |                                                              |
| Compound Assignment Statement       | OK    |                                                              |
| Variable Declaration Statement      | MOVED | Moved responsibility into Variable Declaration Kind.         |
| Block Statement                     | OK    |                                                              |
| Break Statement                     | OK    |                                                              |
| Fail Statement                      | OK    |                                                              |
| Expression Statement                | MOVED | Moved responsibility into Expression Kind.                   |
| Continue Statement                  | OK    |                                                              |
| If Else Statement                   | OK    |                                                              |
| While Statement                     | OK    |                                                              |
| Panic Statement                     | OK    | Similar to throwing an error. Will throw the error and ignore from then. |
| Return Statement                    | OK    |                                                              |
| Local Type Definition Statement     | MOVED | Moved responsibility into Module Member Declaration Kind.    |
| Lock Statement                      | OK    | Atomically change the values of the variables.               |
| Fork Statement                      | OK    | Starts workers. (Might cause problems)                       |
| For Each Statement                  | OK    |                                                              |
| XML Namespace Declaration Statement | MOVED | Moved responsibility into Module Member Declaration Kind.    |
| Transaction Statement               | OK    |                                                              |
| Rollback Statement                  | ERROR | Rollback cannot be used outside of a transaction block.      |
| Retry Statement                     | OK    | Retry can exist outside transactions as a general purpose control. |
| Match Statement                     | OK    | Similar to switch statements.                                |
| Do Statement                        | OK    | ?                                                            |

#### Expression Kind

These are expressions that are executable but returns a value which should be immediately displayed. If the statement contained multiple expressions, only the final expression value will be given.

Service expressions are rejected because they have a side effect of starting a server.

## Annex

### Overview Diagram

![Overview](../docs/overview.png)

### Invoker Template

```ballerina
import ballerina/io as io;
import ballerina/java as java;

<#list imports as import>
${import}
</#list>

function recall(handle context_id, handle name) returns any|error = @java:Method {
    'class: "${memoryRef}"
} external;
function memorize(handle context_id, handle name, any|error value) = @java:Method {
    'class: "${memoryRef}"
} external;
function recall_var(string name) returns any|error {
    return trap recall(context_id, java:fromString(name));
}
function memorize_var(string name, any|error value) {
    memorize(context_id, java:fromString(name), value);
}

<#list moduleDclns as dcln>
${dcln}
</#list>

handle context_id = java:fromString("${contextId}");
${lastVarDcln}

<#list initVarDclns as varNameType>
${varNameType.second} ${varNameType.first} = <${varNameType.second}> recall_var("${varNameType.first}");
</#list>

function save(){
    <#list saveVarDclns as varNameType>
    memorize_var("${varNameType.first}", ${varNameType.first});
    </#list>
}

function run() returns @untainted any|error {
    <#if lastExpr.second>
    ${lastExpr.first}
    return ();
    <#else>
    return trap (${lastExpr.first});
    </#if>
}

public function main() returns error? {
    any|error ${exprVarName} = trap run();
     if (${exprVarName} is error){
        io:println("Exception occurred: ", ${exprVarName}.message());
        return ${exprVarName};
    }
    memorize_var("${exprVarName}", ${exprVarName});
    return trap save();
}
```

## References

[reple: "Replay-Based" REPLs for Compiled Languages](https://people.eecs.berkeley.edu/~brock/blog/reple.php) - A blog post on reple: "Replay-Based" REPLs for Compiled Languages and limitations/fixes possible.

[JShell](https://docs.oracle.com/javase/9/jshell/introduction-jshell.htm#JSHEL-GUID-630F27C8-1195-4989-9F6B-2C51D46F52C8) - A REPL for Java programming language.

[RCRL](https://github.com/onqtam/rcrl) - Read-Compile-Run-Loop: tiny and powerful interactive C++ compiler (REPL)

