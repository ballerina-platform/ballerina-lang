# Language Server Extension Points/ APIs
Ballerina Language Server provides extension points to following editor features. 

- <a href="#SuggestionsAndAutoCompletion">Suggestions and Auto Completion</a>
- <a href="#CodeAction">Code Actions</a>
    - <a href="#CodeActionCommunication">Client-Server Communication</a>
    - <a href="#CodeActionAttachmentPoints">Attachment Points</a>
    - <a href="#CodeActionCommandExecutor">Command Executors</a>

<a name="SuggestionsAndAutoCompletion"></a>

## Suggestions and Auto-Completion
Suggestion and Auto-Completion is triggered while you are typing and also allows you to request suggestions explicitly for a given context with the <kbd>Ctrl</kbd> + <kbd>Space</kbd>. (or <kbd>&#8984; Command</kbd> + <kbd>Space</kbd> in Mac)

<a name="CodeAction"></a>

## Code Action and Quick Fixes

Code Action is triggered when you change your cursor position and also allows you to request code-actions explicitly for a given context with the <kbd>Ctrl</kbd> + <kbd>.</kbd> (or <kbd>&#8984; Command</kbd> + <kbd>.</kbd> in Mac)

<a name="CodeActionCommunication"></a>

### Client-Server Communication
Communication related to the code-actions.
```
Language Client(eg.VSCode)       Language Server
    |                                    |
    |(cursor changes)                    |
    |                                    |
    |        code-action-request         |
    |----------------------------------->|
    |                                    |
    |       code-action-response         |
    |<-----------------------------------|
    |                                    |
    |    command-execution(optional)     |
    |----------------------------------->|
    |                                    |
```
<a name="CodeActionAttachmentPoints"></a>

### Code Action Attachment Points

1. <a href="#CodeActionDiagnostic">Diagnostics based Code Actions</a>
2. <a href="#CodeActionNodeType">Code Action Node-Type</a>
3. <a href="#CodeActionCompilerPlugin">Compiler plugin based Code Actions</a>

<a name="CodeActionDiagnostic"></a>

#### 1. Diagnostic Based Code Action
* Whenever there's a diagnostics message in the current cursor position these type of code-actions are getting triggered.

* Can be implemented using Java Service loader interface `LSCodeActionProvider` [here](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/spi/LSCodeActionProvider.java).
##### Diagnostic Based Code Action Sample
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
<a name="CodeActionNodeType"></a>

#### 2. Node Type Based Code Action
* Whenever the node-type of the current cursor position matches pre-declared node-types, these type of code-actions are getting triggered.

* Supported node-types includes variables, functions, objects, classes, services...etc. The complete list can be found in [here](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/CodeActionNodeType.java)

* Can be implemented using the same Java Service loader interface `LSCodeActionProvider` [here](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/spi/LSCodeActionProvider.java).
#####  Node Type Based Code Action Sample
```java
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddDocumentationCodeAction extends AbstractCodeActionProvider {
    public AddDocumentationCodeAction() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION, CodeActionNodeType.OBJECT));
    }

    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context) {
        // return list of code-actions
    }
```
<a name="CodeActionCompilerPlugin"></a>

#### 3. Compiler Plugin Based Code Actions
* The compiler plugin based code actions allow the developers to write code actions as a compiler plugin, without any prior requirement of the language server knowledge. The language server is going to load the plugins at the runtime and convert them to code actions.
Refer [this](CompilerPluginCodeActionExtensions.md) for detailed explanation.
<a name="CodeActionCommandExecutor"></a>

#### Command Executor

For long-running code-actions you can separate out the execution into a command executor and Ballerina LanguageServer provides SPIs to plug in Command Executors with the `LSCommandExecutor` interface [here](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/command/spi/LSCommandExecutor.java).
##### Command Executor Sample
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
