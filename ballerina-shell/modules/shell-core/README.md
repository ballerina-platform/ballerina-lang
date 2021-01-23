# Ballerina Shell Implementation

## Invoker Implementation

The main logic of the ballerina shell lies in the invoker component. Invoker is the component responsible for executing a snippet. Snippets are the code segments given by the user to evaluate.

```java
public abstract class Invoker extends DiagnosticReporter {
  /**
   * Executes a snippet and returns the output lines.
   * Snippets parameter should only include newly added snippets.
   * Old snippets should be managed as necessary by the implementation.
   *
   * @param newSnippet New snippet to execute.
   * @return Execution output result.
   */
  public abstract Optional<Object> execute(Snippet newSnippet) throws InvokerException;

  // ..
}
```

The main logic of the currently implemented invoker (`ClassLoadInvoker`) can be depicted as below. Before the invoker, the application front-end will take user input and categorize it into the correct type of snippet. The snippet type refers to mainly the location that the code segment will end up in the generated wrapper(template). For example, `ImportSnippets` will go to the topmost section in the generated wrapper. The snippet will then undergo the following operations to be invoked.

- Some **information required for execution will be derived** from the snippet. The used information will be used in the next stage to generate a syntactically correct source.

- The invoker will then **generate the `.bal` file** using the snippet data, the previously defined module-level declarations, and variable details. Information gathered in the previous step is used to create the wrapper required. For example, variable-type information is required for casting statements. Also, to enable imports, only the used imports are selectively included.

- The invoker will then **execute the generated ballerina file**. A custom security manager is employed to stop VM from exiting because of `System.exit()` calls in the ballerina executable. Note that only variable declaration, statement and, expression snippets are executed.

  > TODO: Find a way to bypass the system exits without Security Manager.

- In the execution, the ballerina file will **load/save the variable values from the memory class**. This memory class is a class with a static map that exposes static methods: `recall` and `memorize`.
  - `memorize` will save the variable value as a plain Java Object. `recall` will return the saved object. Since the life-time of this class is persistent throughout every execution phase, the variable values saved will be available for each execution. (So this will act as
    persistent storage for the ballerina global variables)
  - At the start of each execution, all variables are loaded from the Java memory class and cast into the required type. Then the snippet
    is executed and all the variables are saved back in the memory class.

- Additionally, a context id is used to allow several invoker sessions to run. If there are any errors, they are reported as necessary. If the snippet was an expression evaluation, and the result was not-null, the **result will be returned**.

The overview sequence diagram is shown below as well.

![Invoker Seq](../docs/sequence.png)

### Information Gathered from Snippets

#### Import Declarations

Import prefixes will be derived using `SyntaxTree`. This data is used to determine if a certain import is used in a future snippet to selectively include imports. (Because unused imports are not allowed) Some import statements and the prefixes they are imported as are shown below.

```ballerina
import abc/xyz				// prefix: 'xyz
import abc/pqr.xyz			// prefix: 'xyz
import abc/pqr.'xyz			// prefix: 'xyz
import pqr					// prefix: 'pqr
import abc/pqr as xyz		// prefix: 'xyz
```

If the import is valid, it will then be included in every future import usage. To achieve this, each snippet is scanned for import usage using the `SyntaxTree` for `QualifiedNameReferenceNode`s.

```ballerina
// Imports with prefixes 'abc and 'xyz will be included with this snippet.
abc:pqr() + xyz:ABC + mn
```

The validity of import will be determined by compiling the snippet. After compilation, `MODULE_NOT_FOUND` compilation errors suggest that the import is invalid. The import prefix will only be remembered if the import is valid.

#### Module Level Declarations

Module declarations are also compiled similar to how the imports are compiled. If the compilation was completed without any errors, the module declaration will be marked to be included in all future iterations as-is. Also, any new symbols found from the module declaration is remembered. Module-level declarations do not change the state of the application. (Except for variable declarations which are processed under variable declaration snippets) So these are not executed similar to import statements.

Any imports used inside module-level declaration snippets will be marked as `mustImport` as they will be required to be imported regardless of usage in future snippets. Since module-level declarations will be added to the wrapper/template as-is, any import used in a valid module-level declaration will be included in all consequent snippet evaluations.

```ballerina
function f(int x) returns int{
	return abc:P(x);
}

// Import with prefix 'P will be marked as mustImport and
// included in all next imports regardless of its use in the current snippet.
```

#### Variable Declarations

Variable declarations undergo two stages of compilation. The initial compilation is done to find the variable symbol information. The latter compilation is done to execute and save the after-state.

The initial compilation is done similar to module-level declarations. However, the snippets are placed inside a method to enable complex expressions as initializers that would be illegal in a top-level variable declaration and to enable list/array/record binding patterns. The symbols in the method are extracted via `visibleSymbols` API in the `SemanticModel`. The location for symbol finding is taken using `SyntaxTree`. The type will be taken for any `VariableSymbol` or `FunctionSymbol` using their `symbol.typeDescriptor()` method. The type is then traversed and transformed into a string and implicit imports are found that are required to include the type. The main reason for this is the `var` type variables.

```ballerina
var x = abc:X()
// If abc:P returns a type of say, pqr:Y, import with prefix pqr needs 
// to be included implicitly. So the snippet should be converted to,
import pqr
pqr:Y x = abc:X()
```

However, there can be some edge cases unhanded by the current implementation. Also, private types with `var` is not supported since there is currently no implementation to find if a type is not public.

> TODO: Fix bugs of edge cases related to type.

### Snippet Execution

Variable declaration snippets, statement snippets, and expression snippets are executed. The same wrapper/template is used for each snippet. Additionally, some helper functions are also included. (eg: `println`, `sprintf`)

```ballerina
// imports as-is

// module-level-dclns as-is

// previous var-dclns in format
TYPE VAR_NAME = <TYPE> recall_var("VAR_NAME");

function run() returns any|error {
	// stmt or expr
	return // expr value or null if stmt
}

function stmts() returns any|error {
	any|error EXPR = trap run();
	
	// save EXPR and other variable values in format
	memorize_var("VAR_NAME", VAR_NAME);
	
	return EXPR;
}

public function main() returns error? {
	any|error EXPR = trap stmts();
    // print EXPR if EXPR is an error
}
```

## Front-End

![Overview](../docs/overview.png)


### Preprocessor

The preprocessor is the first transformational phase of the program. Any input is sent through the preprocessor to convert the input into a list of individually processable statements. For example, any multiple statement input will be split into the relevant list of string counterpart at the end of this phase. The implemented `SeparatorPreprocessor` currently splits the statements into separated lists depending on the semicolons that are in the root bracket level. The motivation of a preprocessor is to divide the input into separately identifiable sections so each can be individually processed. Removing comments is also a task done by the preprocessor.

```java
public abstract class Preprocessor extends DiagnosticReporter {
  /**
   * Preprocesses the string and output the list of
   * processed outputs.
   *
   * @param input Input string
   * @return Processed resultant strings
   * @throws PreprocessorException If the preprocessing failed.
   */
  public abstract Collection<String> process(String input) throws PreprocessorException;
}
```

Currently implemented `Separator preprocessor` will split the input into several statements based on the semicolons and brackets and remove the comments. Following are some inputs and expected output of the preprocessor for reference.

| Input                                             | Expected Output                                     |
| ------------------------------------------------- | --------------------------------------------------- |
| `int number`                                      | [`int number`]                                      |
| `int number; number = 100;`                       | [`int number;`, `number = 100;`]                    |
| `function () { int p = 100; string h = "hello";}` | [`function () { int p = 100; string h = "hello";}`] |
| `int a = 0; while (a < 10) { a+= 1; }`            | [`int a = 0;`, `while (a < 10) { a+= 1; }`]         |

### Tree Parser

In this stage, the correct syntax tree is identified. The root node of the syntax tree must be the corresponding type for the statement. For example, for an import declaration, the tree that is parsed should have `ImportDeclarationNode` as the root node.

```java
public abstract class TreeParser extends DiagnosticReporter {
  /**
   * Parses a source code string into a Node.
   * Input source code is expected to be a single statement/expression.
   *
   * @param statement Input source code statement.
   * @return Syntax tree for the source code.
   */
  public abstract Node parse(String statement) throws TreeParserException;
}
```

Currently implemented `SerialTreeParser`, will parse the source code line using a trial based method. The source code is placed in several places and is attempted to parse. This continues until the correct type can be determined.

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

Snippets are defined in terms of the source code it contains. Snippets would be of mainly 5 categories. Erroneous snippets are rejected as soon as they are detected. So, there isn't a category for them.

#### Import Declaration Snippets

Snippets that represent an import statement.

#### Variable Declaration Snippet

In the REPL, only module-level variables are allowed. The main motivation of that is to keep a global state. (So every defined variable is available to others.) Thus, `VariableDeclarationNodes` will also be converted into `ModuleVariableDeclarationNode`. However, the ability of variable declarations to not have an initializer will be an issue. Currently, the declaration will be converted as-is. If there isn't an initializer, the compiler will give an error.

> TODO: Assignments done to global variables in closures will not be reflected after the execution. The changes will be visible only for the scope belonging to the snippet where the closure was defined.
>
> ```ballerina
> int x = 10
> var f = function () { x = 12; }
> f()
> x   // <- this should output 12 but will output 10 instead
> ```

#### Module-level Declaration Snippet

These are not executable. They do not execute to return a value. Documentations and public keywords cannot be used in the module-level declarations. Following are the supported module-level declaration types.

| Sub Kind Name                    | State  | Notes                                                      |
| -------------------------------- | ------ | ---------------------------------------------------------- |
| Function Definition              | OK     |                                                            |
| Listener Declaration             | OK     |                                                            |
| Type Definition                  | OK     |                                                            |
| Service Declaration              | ERROR  | Services are not allowed within REPL.                      |
| Constant Declaration             | OK     | Constant variables are always defined at the module level. |
| Module Variable Declaration      | IGNORE | Moved responsibility into Variable Declaration Kind.       |
| Annotation Declaration           | OK     |                                                            |
| Module XML Namespace Declaration | OK     |                                                            |
| Enum Declaration                 | OK     |                                                            |
| Class Definition                 | OK     |                                                            |

#### Statement Kind

These are normal statements that should be evaluated from top to bottom inside a function. Fail Statement Sub Kind is not accepted.

| Sub Kind Name                       | State  | Notes                                                        |
| ----------------------------------- | ------ | ------------------------------------------------------------ |
| Assignment Statement                | OK     |                                                              |
| Compound Assignment Statement       | OK     |                                                              |
| Variable Declaration Statement      | IGNORE | Moved responsibility into Variable Declaration Kind.         |
| Block Statement                     | OK     |                                                              |
| Break Statement                     | OK     |                                                              |
| Fail Statement                      | OK     |                                                              |
| Expression Statement                | IGNORE | Moved responsibility into Expression Kind.                   |
| Continue Statement                  | OK     |                                                              |
| If Else Statement                   | OK     |                                                              |
| While Statement                     | OK     |                                                              |
| Panic Statement                     | OK     |                                                              |
| Return Statement                    | OK     |                                                              |
| Local Type Definition Statement     | IGNORE | Moved responsibility into Module Member Declaration Kind.    |
| Lock Statement                      | OK     | Atomically change the values of the variables.               |
| Fork Statement                      | OK     |                                                              |
| For Each Statement                  | OK     |                                                              |
| XML Namespace Declaration Statement | IGNORE | Moved responsibility into Module Member Declaration Kind.    |
| Transaction Statement               | OK     |                                                              |
| Rollback Statement                  | OK     |                                                              |
| Retry Statement                     | OK     | Retry can exist outside transactions as a general-purpose control. |
| Match Statement                     | OK     | Similar to switch statements.                                |
| Do Statement                        | OK     |                                                              |

#### Expression Kind

These are expressions that are executable but returns a value that should be immediately displayed. If the statement contained multiple expressions, only the final expression value will be given.

Service expressions are rejected because they have a side effect of starting a server.

## References

[reple: "Replay-Based" REPLs for Compiled Languages](https://people.eecs.berkeley.edu/~brock/blog/reple.php) - A blog post on reple: "Replay-Based" REPLs for Compiled Languages and limitations/fixes possible.

[JShell](https://docs.oracle.com/javase/9/jshell/introduction-jshell.htm#JSHEL-GUID-630F27C8-1195-4989-9F6B-2C51D46F52C8) - A REPL for Java programming language.

[RCRL](https://github.com/onqtam/rcrl) - Read-Compile-Run-Loop: tiny and powerful interactive C++ compiler (REPL)

