# Language Server Extension Points/ APIs
Ballerina Language Server provides extension points to following editor features. 

- <a href="#SuggestionsAndAutoCompletion">Suggestions and Auto Completion</a>
- <a href="#CodeAction">Code Actions</a>
    - <a href="#CodeActionCommunication">Client-Server Communication</a>
    - <a href="#CodeActionAttachmentPoints">Attachment Points</a>
    - <a href="#CodeActionCommandExecutor">Command Executors</a>

<a name="SuggestionsAndAutoCompletion"></a>

## Suggestions and Auto-Completion
Suggestion and Auto-Completion is triggered while you are typing and also allows you to request suggestions explicitly for a given context with the <kbd>Ctrl</kbd> + <kbd>Space</kbd>. (<kbd>&#8984; Command</kbd> + <kbd>i</kbd> or <kbd>&#8997; option</kbd> + <kbd>esc</kbd> in Mac)

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
2. <a href="#CodeActionRange">Range based Code Actions</a>
3. <a href="#CodeActionCompilerPlugin">Compiler plugin based Code Actions</a>

<a name="CodeActionDiagnostic"></a>

#### 1. Diagnostic Based Code Action
* Whenever there's a diagnostics message in the current cursor position(or within the highlighted range) these type of code-actions are getting triggered.

* Can be implemented using [interface](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/spi/DiagnosticBasedCodeActionProvider.java) `DiagnosticBasedCodeActionProvider`
and loaded using Java Service loader [interface]((https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/spi/LSCodeActionProvider.java)) `LSCodeActionProvider`.
##### Diagnostic Based Code Action Sample
```java
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class CreateVariableCodeAction implements DiagnosticBasedCodeActionProvider {
    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {
        // return list of code-actions
    }
}
```
<a name="CodeActionRange"></a>

#### 2. Range Based Code Action
* Whenever the syntaxKind of the selected range(highlighted code range) matches pre-defined set of scenarios, these type of code-actions are getting triggered.

* Can be implemented using [interface](https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/spi/RangeBasedCodeActionProvider.java) `RangeBasedCodeActionProvider`
  and loaded using Java Service loader [interface]((https://github.com/ballerina-platform/ballerina-lang/blob/master/language-server/modules/langserver-commons/src/main/java/org/ballerinalang/langserver/commons/codeaction/spi/LSCodeActionProvider.java)) `LSCodeActionProvider`.

#####  Range Based Code Action Sample
```java
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddDocumentationCodeAction extends RangeBasedCodeActionProvider {
    public getSyntaxKinds() {
        return Arrays.asList(SyntaxKind.FUNCTION_DEFINITION, SyntaxKind.OBJECT_TYPE_DESC);
    }

    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        // return list of code-actions
    }
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
