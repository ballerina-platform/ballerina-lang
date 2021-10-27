# Language Server Code Action Extension via Compiler Plugins

## Overview

The main goal is to allow ballerina packages to write their own code actions. Mainly the standard libraries have this
requirement. With this introduction, standard libraries have the capability to show necessary code actions where
required and apply desired changes once the user clicks on them.

A code action operates in 2 stages:

1. Showing the codeaction - This is when the users see the "bulb" icon in VSCode. It will have a title like
   `Create variable`, `Create function` or `Document this`.

2. Executing the codeaction - This is when the users click on the title mentioned above. Changes (to the source code)
   required to achieve the task mentioned in the "title" in previous stage should be done here.

Codeactions are tightly coupled with diagnostics. That is, a compiler plugin should use `CodeAnalyzer`s to check source
code and semantic model to determine
**errors, best practice violations or possible improvements**. Then, the `CodeAnalyzer` should report diagnostics for
the identified places. You can set the `DiagnisticSeverity` to `INTERNAL` if that diagnostic is only for the use of
codeactions. You can use `DiagnosticProperty` to pass additional details required by the codeactions to proceed.

Following sections describe how codeactions should be implemented in detail.

This guide assumes that you are already familiar with compiler plugins.

## Introduction to `CodeAction` interface

The `CodeAction` interface looks like below:

```java
/**
 * Represents an action (named <strong>code action</strong> that can be performed by the user when writing ballerina
 * code using an IDE/editor with language server support. Usually these are used to provide quick fixes for
 * diagnostics. This interface provides methods to get details of the code action for a provided diagnostic and to
 * apply/execute the code action when the user clicks it.
 *
 * @since 2.0.0
 */
public interface CodeAction {

    /**
     * Set of interested diagnostic codes for this code action. This code action will be invoked only for diagnostics
     * with matching diagnostic codes.
     *
     * @return List of interested diagnostic codes
     */
    List<String> supportedDiagnosticCodes();

    /**
     * Returns the details of the code action depending on the current context (cursor position, surrounding syntax tree
     * nodes, etc) and diagnostic information.
     *
     * @param context Context representing the document, syntax tree, cursor position and etc
     * @return Optional code action details. Optional can be empty if the code action doesn't apply to the context
     */
    Optional<CodeActionInfo> codeActionInfo(CodeActionContext context);

    /**
     * Once the user accepts the quickfix (code action), this method is invoked to perform the required changes to the
     * document(s).
     *
     * @param context Code action context
     *                returning this code action
     * @return A list of document edits to be applied to the ballerina files in the project
     */
    List<DocumentEdit> execute(CodeActionExecutionContext context);

    /**
     * A unique name (within the compiler plugin) representing this code action. It will be used to uniquely identify
     * this code action during runtime.
     *
     * @return A unique (within the compiler plugin) name
     */
    String name();
}
```

1. `supportedDiagnosticCodes()`- An implementation should return the diagnostic codes the implemented codeaction is
   supposed to handle.

2. `codeActionInfo` - This method gets invoked with the diagnostic (its diagnostic code will be one from the above
   method). You are supposed to return the **title** of the codeaction at the end of this method.

3. `execute()` - Gets invoked when the user clicks the codeaction. Should return the text edits required to be made to
   the source code.

4. `name()` - Every codeaction should have a unique name (within the compiler plugin only).

## Example

Let's consider a scenario where we want to suggest a code action titled `Add resource function` if a `service` doesn't
have any `resource function`.

### Step 1 - Publishing Diagnostics

You can write a codeaction to respond to an existing diagnostic which is published by the compiler or publish a new
diagnostic after performing desired checks within the `CodeAnalizer`.

In this scenario, within `CodeAnalyzer`, we have to check if the service doesn't have any resource functions.

```java
public class ServiceValidator implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext syntaxNodeAnalysisContext) {
        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) syntaxNodeAnalysisContext.node();
        NodeList<Node> members = serviceDeclarationNode.members();
        if (members.isEmpty()) {
            DiagnosticInfo info = new DiagnosticInfo("SERVICE_001", "No members in service", DiagnosticSeverity.INTERNAL);
            // We set service location as the diagnostic location
            syntaxNodeAnalysisContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(info, serviceDeclarationNode.location()));
        }
    }
}

```

If no members are present, we add the diagnostic (type `INTERNAL` since this is not an error and not relevant to end
users). Note the diagnostic code `SERVICE_001`.

### Step 2 - Writing the code action

Following source code shows how to write the codeaction. It contains inline comments explaining what is done and how to.
A utility class (or method) will be required to find nodes from the syntax tree given the node location.

```java
/**
 * A utility class containing methods required to find nodes and etc.
 */
public class CodeActionUtil {

    public static final String NODE_LOCATION_KEY = "node.location";

    private CodeActionUtil() {
    }

    /**
     * Find a node in syntax tree by line range.
     *
     * @param syntaxTree Syntax tree
     * @param lineRange  line range
     * @return Node
     */
    public static NonTerminalNode findNode(SyntaxTree syntaxTree, LineRange lineRange) {
        if (lineRange == null) {
            return null;
        }

        TextDocument textDocument = syntaxTree.textDocument();
        int start = textDocument.textPositionFrom(lineRange.startLine());
        int end = textDocument.textPositionFrom(lineRange.endLine());
        return ((ModulePartNode) syntaxTree.rootNode()).findNode(TextRange.from(start, end - start), true);
    }
}

/**
 * Our code action implementation
 */
public class AddResourceMethod implements CodeAction {

    @Override
    public List<String> supportedDiagnosticCodes() {
        // We are interested in SERVICE_001 diagnostic
        return List.of("SERVICE_001");
    }

    @Override
    public Optional<CodeActionInfo> codeActionInfo(CodeActionContext context) {
        Diagnostic diagnostic = context.diagnostic();

        // Arguments (should be json serializable) are passed to execute() method when user clicks on this code action
        // An argument has a key (string) and a value (anything serializable)
        // Here, we pass the diagnostic location (= service declaration node's line range) as an argument, because we
        //   need it when executing the code action.
        CodeActionArgument locationArg = CodeActionArgument.from(CodeActionUtil.NODE_LOCATION_KEY,
                diagnostic.location().lineRange());
        // Return codeaction with desired title and arguments
        return Optional.of(CodeActionInfo.from("Add resource function", List.of(locationArg)));
    }

    @Override
    public List<DocumentEdit> execute(CodeActionExecutionContext context) {
        // Obtain line range from the command arguments. We send it in the above method
        LineRange lineRange = null;
        for (CodeActionArgument argument : context.arguments()) {
            if (CodeActionUtil.NODE_LOCATION_KEY.equals(argument.key())) {
                lineRange = argument.valueAs(LineRange.class);
            }
        }

        if (lineRange == null) {
            return Collections.emptyList();
        }

        SyntaxTree syntaxTree = context.currentDocument().syntaxTree();
        // Find the node from syntax tree using the line range
        NonTerminalNode node = CodeActionUtil.findNode(syntaxTree, lineRange);
        if (!(node instanceof ServiceDeclarationNode)) {
            return Collections.emptyList();
        }

        // Get the service declaration node
        ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
        List<TextEdit> textEdits = new ArrayList<>();

        // Resource function is usually 4 spaces indented
        String padding = " ".repeat(serviceDeclarationNode.lineRange().startLine().offset() + 4);
        // Text for resource function
        String insertText = "\n" + padding + "resource function get path1() {\n" + padding + "\n" + padding + "}";

        // We add the new text (function) after the open brace of the service declaration
        TextRange textRange = TextRange.from(serviceDeclarationNode.openBraceToken().textRange().endOffset(), 0);
        textEdits.add(TextEdit.from(textRange, insertText));
        // Create a text document change with our edits
        TextDocumentChange change = TextDocumentChange.from(textEdits.toArray(new TextEdit[0]));
        // Modify the existing text document with the change
        TextDocument modifiedTextDocument = syntaxTree.textDocument().apply(change);
        // Create the new syntax tree with the new text document
        SyntaxTree modifiedSyntaxTree = SyntaxTree.from(modifiedTextDocument);

        // Create a document edit. Represents an edit to a specific document in the workspace. Document is specified
        //  by the fileUri. Need to provide the modified syntax tree.
        DocumentEdit documentEdit = new DocumentEdit(context.fileUri(), modifiedSyntaxTree);

        // Return a list of document changes. This means, a codeaction can edit multiple documents.
        return List.of(documentEdit);
    }

    @Override
    public String name() {
        // Name given to this codeaction
        return "ADD_RESOURCE_FUNCTION";
    }
}
```

#### Note

Do not use `SyntaxTree.from(oldSyntaxTree, change)` to create the new syntax tree due to 
https://github.com/ballerina-platform/ballerina-lang/issues/24058. It may cause an out of memory issue in the runtime.

### Step 3 - Manual Testing

To do manual testing whether the added codeactions are working, you have to,

1. Publish the ballerina package to local repository
2. In `Ballerina.toml` add a dependency to this package with `repository = "local"`

### Unit Testing

In unit testing, we have to test 2 things:

1. Given the cursor position on a source file, check if the expected codeaction is returned by our CodeAction.
2. Assuming that the correct codeaction was returned, test the `DocumentEdit`s returned by the codeaction once executed.
   That is, make sure the expected document(or documents) was updated as expected.

While you can decide how exactly is to perform the checks, we suggest using the following methods.

#### Get Codeactions for given cursor position

**Note: The cursor position should be within the line range of a diagnostic listed by
`CodeAction.supportedDiagnosticCodes()` for a given codeaction to be invoked by the language server.**

To simulate the above diagnostic line range constraint, a `withinRange()` check is used to filter diagnostics in the
following code.

```java
public class CodeActionUtils {

    /**
     * Get codeactions for the provided cursor position in the provided source file.
     *
     * @param filePath  Source file path
     * @param cursorPos Cursor position
     * @param project   Project
     * @return List of codeactions for the cursor position
     */
    public static List<CodeActionInfo> getCodeActions(Path filePath, LinePosition cursorPos, Project project) {
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();
        // Codeaction manager is our interface to obtaining codeactions and executing them
        CodeActionManager codeActionManager = compilation.getCodeActionManager();

        DocumentId documentId = project.documentId(filePath);
        Document document = currentPackage.getDefaultModule().document(documentId);

        return compilation.diagnosticResult().diagnostics().stream()
                // Filter diagnostics available for the cursor position
                .filter(diagnostic -> CodeActionUtil.isWithinRange(diagnostic.location().lineRange(), cursorPos) &&
                        filePath.endsWith(diagnostic.location().lineRange().filePath()))
                .flatMap(diagnostic -> {
                    CodeActionContextImpl context = CodeActionContextImpl.from(
                            filePath.toUri().toString(),
                            filePath,
                            cursorPos,
                            document,
                            compilation.getSemanticModel(documentId.moduleId()),
                            diagnostic);
                    // Get codeactions for the diagnostic
                    return codeActionManager.codeActions(context).stream();
                })
                .collect(Collectors.toList());
    }
}
```

This method accepts the source file path, cursor position and the project as arguments. You can get rid of the `project`
agument and use `ProjectLoader.loadProject()` with the source file path as a parameter to load the project.

Once you have the list of codeactions, you can simply check for the _title_ and _arguments_. Note that the
`CodeActionInfo` object has another attribute named `providerName`. This is for internal usage and formed in the
following manner:

```
${DiagnosticCode}/${BalPackageOrgName}/${BalPackagePackageName}/${CodeAction.name()}
```

Here's an example:

```jshelllanguage
    // Load project
    Project project = ProjectLoader.loadProject(filePath, getEnvironmentBuilder());
    // Get codeactions for cursor position and assert
    List < CodeActionInfo > codeActions = CodeActionUtils.getCodeActions(filePath, cursorPos, project);
    Assert.assertTrue(codeActions.size() > 0, "Expect atleast 1 code action");

    // Compare expected codeaction and received codeactions to check if we got the expected codeaction
    // Here, we compare codeactions using JSON serialization. You can perform this manually by comparing each
    // field of a codeaction as well.
    JsonObject expectedCodeAction = GSON.toJsonTree(expected).getAsJsonObject();
    Optional < CodeActionInfo > found = codeActions.stream()
            .filter(codeActionInfo -> {
                JsonObject actualCodeAction = GSON.toJsonTree(codeActionInfo).getAsJsonObject();
                return actualCodeAction.equals(expectedCodeAction);
            })
            .findFirst();
    Assert.assertTrue(found.isPresent(), "Codeaction not found:" + expectedCodeAction.toString());

```

#### Execute a codeaction

You can use the following method to execute a codeaction and get the `DocumentEdit` list returned.

```java
public class CodeActionUtils {

    /**
     * Get the document edits returned when the provided codeaction is executed.
     *
     * @param project    Project
     * @param filePath   Source file path
     * @param codeAction Codeaction to be executed
     * @return List of edits returned by the codeaction execution
     */
    private List<DocumentEdit> executeCodeAction(Project project, Path filePath, CodeActionInfo codeAction) {
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();

        DocumentId documentId = project.documentId(filePath);
        Document document = currentPackage.getDefaultModule().document(documentId);

        // Need to convert the arguments to JsonElement because internally we convert
        // JsonElements to objects via deserialization.
        List<CodeActionArgument> codeActionArguments = codeAction.getArguments().stream()
                .map(arg -> CodeActionArgument.from(GSON.toJsonTree(arg)))
                .collect(Collectors.toList());

        CodeActionExecutionContext executionContext = CodeActionExecutionContextImpl.from(
                filePath.toUri().toString(),
                filePath,
                null,
                document,
                compilation.getSemanticModel(document.documentId().moduleId()),
                codeActionArguments);

        // Execute and get documents edits for the codeaction
        return compilation.getCodeActionManager()
                .executeCodeAction(codeAction.getProviderName(), executionContext);
    }
}
```

Since a `DocumentEdit` contains the `fileUri` and `SyntaxTree` (modified), you can perform any checks upon that to see
if the expected changes were applied. For example, you can compare the expected source code
with `syntaxTree.toSourceCode()`
which is the simplest approach.

```jshelllanguage
    List < DocumentEdit > actualEdits = CodeActionUtils.executeCodeAction(project, filePath, found.get());
    // Assert the number of files supposed to be changed
    // A DocumentEdit is for a single document.
    Assert.assertEquals(actualEdits.size(), n, "Expected changes to n files");

    // Check if expected codeaction is there
    String expectedFileUri = filePath.toUri().toString();
    Optional < DocumentEdit > actualEdit = actualEdits.stream()
            .filter(docEdit -> docEdit.getFileUri().equals(expectedFileUri))
            .findFirst();

    Assert.assertTrue(actualEdit.isPresent(), "Edits not found for fileUri: " + expectedFileUri);

    // Compare expected source code and actual source code
    String modifiedSourceCode = actualEdit.get().getModifiedSyntaxTree().toSourceCode();
    String expectedSourceCode = Files.readString(expectedSrc);
    Assert.assertEquals(modifiedSourceCode, expectedSourceCode,
            "Actual source code didn't match expected source code");
```
