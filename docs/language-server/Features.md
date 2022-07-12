# Ballerina Language Server Features

Ballerina Language Server provides the following editor features.

* [1. Completions](#completions)
* [2. Code Actions](#code-actions)
    * [2.1. Editor-Language Server Communication](#editor-language-server-communication)
    * [2.2. Code Action Types](#code-action-types)
      * [2.2.1. Diagnostics based Code Actions](#21-diagnostic-based-code-actions) 
      * [2.2.2. Node based Code Actions](#22-node-based-code-actions) 
      * [2.2.3. Compiler Plugin Code Actions](#23-compiler-plugin-based-code-actions) 
    * [2.3. Command Executors](#command-executors)
* Rename
* Goto Definition
* Signature Help
* Formatting
* Hover
* Text Synchronization
* Semantic Tokens

## Completions

Suggestions and Auto-Completions are triggered while you are typing and also allows you to request suggestions
explicitly for a given context with the <kbd>Ctrl</kbd> + <kbd>Space</kbd>. (or <kbd>&#8984; Command</kbd> + <kbd>
Space</kbd> in Mac)

## Code Actions

Code Action is triggered when you change your cursor position and also allows you to request code-actions explicitly for
a given context with the <kbd>Ctrl</kbd> + <kbd>.</kbd> (or <kbd>&#8984; Command</kbd> + <kbd>.</kbd> in Mac)

### Editor-Language Server Communication

Communication related to the code-actions.

```mermaid
sequenceDiagram
  participant E as Editor (ex: VSCode)
  participant LC as Language Client
  participant LS as Language Server

  activate E
  E ->> E: cursor position changes
  activate LC
  E ->> LC: get code actions
  
  activate LS
  LC ->> LS: code action request
  LS ->> LC: code action response
  deactivate LS
  
  LC ->> E: show code actions
  deactivate LC
  E ->> E: user clicks a code action
  activate LC
  E ->> LC: execute code action
  Note over E, LC: A code action may have <br/>the text edits or a command
  alt text edits available
    LC ->> E: apply text edits 
  end
    
  alt command available
    activate LS
    LC -) LS: execute command request
    LS -->> LC: workspace edits  
    deactivate LS
  end
  
  LC ->> E: apply workspace edit
  deactivate LC
  deactivate E
```

### Code Action Types

There are 3 types of code actions in the Ballerina Language Server.

1. [Diagnostics based Code Actions](#21-diagnostic-based-code-actions)
2. [Node based Code Actions](#22-node-based-code-actions)
3. [Compiler plugin Code Actions](#23-compiler-plugin-based-code-actions)

#### 2.1. Diagnostic Based Code Actions

* Whenever there's a diagnostics message in the current cursor position these type of code-actions are getting
  triggered.

* Can be implemented using Java Service loader
  interface `LSCodeActionProvider` [here](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/spi/LSCodeActionProvider.java)

An example:

```java

@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateVariableCodeAction extends AbstractCodeActionProvider {

    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    CodeActionContext context) {
        // return list of code-actions
    }
}
```

#### 2.2. Node Based Code Actions

* Whenever the node-type of the current cursor position matches pre-declared node-types, these type of code-actions are
  getting triggered.

* Supported node-types includes variables, functions, objects, classes, services...etc. The complete list can be found
  in [here](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/CodeActionNodeType.java)

* Can be implemented using the same Java Service loader
  interface `LSCodeActionProvider` [here](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/spi/LSCodeActionProvider.java)

An example:

```java

@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddDocumentationCodeAction extends AbstractCodeActionProvider {

  public AddDocumentationCodeAction() {
    super(Arrays.asList(CodeActionNodeType.FUNCTION, CodeActionNodeType.OBJECT));
  }

  public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context) {
    // return list of code-actions
  }
}
```

#### 2.3. Compiler Plugin Based Code Actions

The compiler plugin based code actions allow the developers to write code actions as a compiler plugin, without any
prior requirement of the language server knowledge. The language server is going to load the plugins at the runtime and
convert them to code actions. Refer [this](CompilerPluginCodeActions.md) for detailed explanation.

### Command Executors

For long-running code-actions you can separate out the execution into a command executor and Ballerina LanguageServer
provides SPIs to plug in Command Executors with the `LSCommandExecutor`
interface [here](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/command/spi/LSCommandExecutor.java)
.

An Example:

```java
public interface LSCommandExecutor {

    Object execute(ExecuteCommandContext context) throws LSCommandExecutorException {
        return new ApplyWorkspaceEditParams();
    }

    String getCommand() {
        return "ballerina.execute.add-docs";
    }
}
```
