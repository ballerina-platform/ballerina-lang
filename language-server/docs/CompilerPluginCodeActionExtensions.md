# Language Server Code Action Extension via Compiler Plugins

## Overview

The main goal is to allow ballerina packages to write their own code actions. Mainly the standard libraries have this
requirement. With this introduction, standard libraries have the capability to show necessary code actions where
required and apply desired changes once the user click them.

A code action operates in 2 stages:

1. Showing the codeaction - This is when the users see the "bulb" icon in VSCode. It will have a title like
   `Create variable`, `Create function` or `Document this`.

2. Executing the codeaction - This is when the user clicks on the title mentioned above. Changes (to the source code)
   required to achieve the task mentioned in the "title" in previous stage should be done here.

Codeactions are tightly coupled with diagnostics. That is, a compiler plugin should use `CodeAnalyzer`s to check source
code and semantic model to determine
**errors, best practice violations or possible improvements**. Then, the `CodeAnalyzer` should report diagnostics for
the identified places. You can set the `DiagnisticSeverity` to `INTERNAL` if that diagnostic is only for the use of
codeactions. You can use `DiagnosticProperty` to pass additional details required by the codeactions to proceed.

Following sections describe how codeactions should be implemented in detail.

This guide assumes that you already are familiar with compiler plugins.

## Introduction to `CodeAction` interface

The `CodeAction` interface looks like below:

```java
public interface CodeAction {

    List<String> supportedDiagnosticCodes();

    Optional<CodeActionInfo> codeActionInfo(CodeActionContext context);

    List<DocumentEdit> execute(CodeActionExecutionContext context);

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

        // Arguments (should be json serializable) are received to execute() method when user click this code action
        // An argument have a key (string) and a value (anything serializable)
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
        // Create the new syntax tree after applying the changes
        SyntaxTree modifiedSyntaxTree = SyntaxTree.from(syntaxTree, change);

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

### Step 3 - Manual Testing

To do manual testing whether the added codeactions are working, you have to,

1. Publish the ballerina package to local repository
2. In `Ballerina.toml` add a dependency to this package with `repository = "local"`

### Unit Testing

_??? Need more information here ??? _

```java
public class CodeActionTest {

    @Test
    public void testCodeActions() {
        // Write a ballerina project which imports your package
        Path projectRoot = RESOURCE_DIRECTORY.resolve("sample_codeaction_package_1");
        // Load the project
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectRoot);
        
        Path filePath = projectRoot.resolve("service.bal");
        DocumentId documentId = project.documentId(filePath);

        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();
        CodeActionManager codeActionManager = compilation.getCodeActionManager();

        Document document = currentPackage.getDefaultModule().document(documentId);

        LinePosition cursorPos = LinePosition.from(20, 65);
        List<CodeActionInfo> codeActions = compilation.diagnosticResult().diagnostics().stream()
                .filter(diagnostic -> CodeActionUtil.isWithinRange(diagnostic.location().lineRange(), cursorPos) &&
                        filePath.endsWith(diagnostic.location().lineRange().filePath()))
                .flatMap(diagnostic -> {
                    CodeActionContextImpl context = CodeActionContextImpl.from(filePath.toUri().toString(),
                            filePath,
                            cursorPos,
                            document,
                            compilation.getSemanticModel(documentId.moduleId()),
                            diagnostic);
                    return codeActionManager.codeActions(context).stream();
                })
                .collect(Collectors.toList());

        Assert.assertTrue(codeActions.size() > 0);
    }
}
```