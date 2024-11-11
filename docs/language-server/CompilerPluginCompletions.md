# Compiler Plugin Completions

## Overview

The main goal of this feature is to allow ballerina packages to provide contextual completion items. With this introduction, compiler plugins of standard libraries will have the capability to show a set of completion items 
in a completion context and apply code snippets when a Ballerina developer selects those completion items.

### Introduction to interfaces

#### `CompletionProvider`

The `CompletionProvider` interface defined in `io.ballerina.projects.plugins.completion` package is the main interface that represents a completion item provider. Each completion item provider will be attached to a set of specific syntax nodes. 

```java
/**
 * Interface for completion item providers.
 *
 * @param <T> generic syntax tree node.
 * @since 2201.7.0
 */
public interface CompletionProvider<T extends Node> {

    /**
     * Get the name of the completion provider.
     *
     * @return Name of the completion provider
     */
    String name();

    /**
     * Calculates and return completion items for the given completion context.
     *
     * @param context completion operation Context
     * @param node    Node instance for the parser context
     * @return  List of calculated Completion Items
     */
    List<CompletionItem> getCompletions(CompletionContext context, T node) throws CompletionException;

    /**
     * Get the attachment points where the current provider attached to.
     *
     * @return List of attachment points
     */
    List<Class<T>> getSupportedNodes();
}

```
1. `name()`: The name of the completion provider.

2. `getCompletions()`: The method that returns the list of completion items for a given completion context.

3. `getSupportedNodes()`: The method that returns the list of classes of syntax nodes that the completion provider supports. This is used to filter out the completion providers that are not relevant to the current completion context.


#### `CompletionItem`

```java
/**
 * Represents a completion item.
 *
 * @since 2201.7.0
 */
public class CompletionItem {
    /**
     * The label of this completion item. By default, also the text that is inserted when selecting
     * this completion.
     */
    private String label;
    
    /**
     * Indicates the priority(sorted position) of the completion item.
     */
    private Priority priority;

    /**
     * An optional array of additional text edits that are applied when selecting this completion. 
     * Edits must not overlap (including the same insert position) with the main edit nor with themselves.
     * Additional text edits should be used to change text unrelated to the 
     * current cursor position (for example adding an import statement at the top of the file if the completion
     * item will insert a qualified type).
     */
    private List<TextEdit> additionalTextEdits;

    /**
     * A string that should be inserted a document when selecting this completion. 
     * When omitted or empty, the label is used as the insert text for this item.
     */
    private String insertText;
    
    public CompletionItem(String label, String insertText, Priority priority) {
        this.label = label;
        this.insertText = insertText;
        this.priority = priority;
    }

    public String getInsertText() {
        return insertText;
    }

    public String getLabel() {
        return label;
    }
    
    public String getFilterText() {
        return filterText;
    }

    public void setFilterText(String filterText) {
        this.filterText = filterText;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setAdditionalTextEdits(List<TextEdit> additionalTextEdits) {
        this.additionalTextEdits = additionalTextEdits;
    }

    public List<TextEdit> getAdditionalTextEdits() {
        return additionalTextEdits;
    }

    /**
     * Represents the priority of the completion item. If priority is high the completion item
     * will be sorted to the top of the completion item list. If low a default priority based on
     * the completion item kind (Snippet) will be assigned.
     */
    public enum Priority {
        HIGH,
        LOW
    }
}
```

A completion item has several properties. As a compiler plugin developer, you can define the following properties of a completion item:

- `label`: The label of the completion item. This is the text displayed in the list of completion items.

- `insertText`: A string that should be inserted a document when the completion item is selected.

- `priority`: The priority of this item defines the order in which the items are shown to the user. If the priority is high the item will be shown before the item will be grouped at the top of the completion item list.

- `additionalTextEdits`: An optional array of additional text edits that are applied when selecting this completion.Edits must not overlap (including the same insert position) with the main edit nor with themselves. Additional text edits should be used to change text unrelated to the  current cursor position (for example adding an import statement at the top of the file if the completion item will insert a qualified type).

## Example
We are going to develop a package called `lstest/package_comp_plugin_with_completions`. We will develop a compiler plugin called `SampleCompilerPluginWithCompletionProvider` with this package. 
Let's consider a scenario where the compiler plugin developer wants to provide a completion item for a `resource function snippet` if one is already not present inside a service which is using a listener defined in the `lstest/package_comp_plugin_with_completions` module. 

Step 1: Develop the `lstest/package_comp_plugin_with_completions` package

#### `main.bal`

```Ballerina
public class Listener {

    private int port = 0;

    public function 'start() returns error? {
        return self.startEndpoint();
    }

    public function gracefulStop() returns error? {
        return ();
    }

    public function immediateStop() returns error? {
        error err = error("not implemented");
        return err;
    }

    public function attach(service object {} s, string[]|string? name = ()) returns error? {
        return self.register(s, name);
    }

    public function detach(service object {} s) returns error? {
        return ();
    }

    public function init(int port) {
    }

    public function initEndpoint() returns error? {
        return ();
    }

    function register(service object {} s, string[]|string? name) returns error? {
        return ();
    }

    function startEndpoint() returns error? {
        return ();
    }
}
```

#### `CompilerPlugin.toml`

```toml
[plugin]
class = "io.ballerina.plugins.completions.CompilerPluginWithCompletionProviders"

[[dependency]]
path = "compiler-plugin-with-completion-provider-1.0.0.jar"
```

Step 2: Implement the CompletionProvider

You can write a completion provider implementing the `CompletionProvider` interface as follows:

```java
/**
 * An example of a completion provider that adds a resource function to a service declaration.
 *
 * @since 2201.7.0
 */
public class ServiceBodyContextProvider implements CompletionProvider<ServiceDeclarationNode> {

    @Override
    public String name() {
        return "ServiceBodyContextProvider";
    }

    @Override
    public List<CompletionItem> getCompletions(CompletionContext context, ServiceDeclarationNode node) throws CompletionException {
        //Adds a resource function if one is not present
        if (node.members().stream().anyMatch(member -> member.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
            return Collections.emptyList();
        }

        String insertText = "resource function " + CompletionUtil.getPlaceHolderText(1, "get") + " "
                + CompletionUtil.getPlaceHolderText(2, "foo") + "(" + CompletionUtil.getPlaceHolderText(3) + ")" +
                " returns " + CompletionUtil.getPlaceHolderText(4, "string") + " {" + CompletionUtil.LINE_BREAK +
                CompletionUtil.PADDING + "return " + CompletionUtil.getPlaceHolderText(5, "\"\"") + ";"
                + CompletionUtil.LINE_BREAK + "}";
        String label = "resource function get foo() returns string";

        CompletionItem completionItem = new CompletionItem(label, insertText, CompletionItem.Priority.HIGH);
        return List.of(completionItem);
    }

    @Override
    public List<Class<ServiceDeclarationNode>> getSupportedNodes() {
        return List.of(ServiceDeclarationNode.class);
    }
}
```

Step 3: Develop the compiler plugin called `CompilerPluginWithCompletionProvider` and add the `CompletionProvider` to the `CompilerPluginContext`.

```java
/**
 * Compiler plugin for testing completion providers.
 *
 * @since 2201.7.0
 */
public class CompilerPluginWithCompletionProvider extends CompilerPlugin {

    @Override
    public void init(CompilerPluginContext pluginContext) {
        pluginContext.addCompletionProvider(new ServiceBodyContextProvider());
    }
}
```

Step 4: Manual testing
1. Publish the ballerina package(with compiler plugin) to local repository
2. Create a new ballerina package with the following source code and in `Ballerina.toml` add a dependency to the `lstest/package_comp_plugin_with_completions` package with `repository = "local"`

#### `main.bal`

```Ballerina
import lstest/package_comp_plugin_with_completions as foo;

public listener listener1 = new foo:Listener(9090);

service on listener1 {
function name() {
    re<cursor>
}
```        

#### `Ballerina.toml`
```toml
[package]
org = "test"
name = "completion_test"
version = "0.1.0"

[[dependency]]
org="lstest"
name="package_comp_plugin_with_completions
repository="local"
version="0.1.0"
```

3. Check if you get the following completion item when you trigger for completion items in the depicted cursor position.

[completions](./images/compiler-plugin-completions-1.png)

Step 4: Unit testing

In unit testing, we have to test the list of completion items given the cursor position on a source file. 

While you can decide how exactly is to perform the checks, we suggest using the following methods.

```java
public class CompletionTestUtils {
    

    /**
     * Get completions for the provided cursor position in the provided source file.
     *
     * @param filePath  Source file path
     * @param cursorPos Cursor position
     * @param project   Project
     * @return List of CompletionItem for the cursor position
     */
    public static List<io.ballerina.projects.plugins.completion.CompletionItem> getCompletions(Path filePath, LinePosition cursorPos, Project project) {
        Package currentPackage = project.currentPackage();
        PackageCompilation compilation = currentPackage.getCompilation();
        // Completion manager is our interface to obtaining completions and executing them
        CompletionManager completionManager = compilation.getCompletionManager();


        DocumentId documentId = project.documentId(filePath);
        Document document = currentPackage.getDefaultModule().document(documentId);
        SemanticModel semanticModel = compilation.getSemanticModel(documentId.moduleId());

        //Position information
        TextDocument textDocument = document.textDocument();
        int cursorPositionInTree = textDocument.textPositionFrom(LinePosition.from(cursorPos.line(), cursorPos.offset()));
        TextRange range = TextRange.from(cursorPositionInTree, 0);
        NonTerminalNode nodeAtCursor = ((ModulePartNode) document.syntaxTree().rootNode()).findNode(range);

        //Create the completion context
        CompletionContextImpl completionContext = CompletionContextImpl.from(filePath.toUri().toString(),
                filePath, cursorPos, cursorPositionInTree, nodeAtCursor, document, semanticModel);
        CompletionResult result = completionManager.completions(completionContext);

        return result.getCompletionItems();
    }
}
```

This method accepts the source file path, cursor position and the project as arguments. You can get rid of the `project`
argument and use `ProjectLoader.loadProject()` with the source file path as a parameter to load the project.

Once you have the list of completion items, you can simply check for the _label_, _insertText_, and _priority_.

Here's an example:

```jshelllanguage

    List <io.ballerina.projects.plugins.completion.CompletionItem > expectedList = getExpectedList();
    // Get completions for cursor position and assert 
    List <io.ballerina.projects.plugins.completion.CompletionItem > completionItems = CompletionUtils.getCodeActions(filePath, cursorPos, project);
    Assert.assertTrue(completionItems.size() > 0, "Expect at least 1 completion item");

    // Compare expected completion item list and received completion item list
    for (int i = 0; i < expectedList.size(); i++) {
        io.ballerina.projects.plugins.completion.CompletionItem expectedItem = expectedList.get(i);
        io.ballerina.projects.plugins.completion.CompletionItem actualItem = completionItems.get(i);
        Assert.assertEquals(actualItem.label(), expectedItem.label(), "Label mismatch");
        Assert.assertEquals(actualItem.insertText(), expectedItem.insertText(), "Insert text mismatch");
        Assert.assertEquals(actualItem.priority(), expectedItem.priority(), "Priority mismatch");

        // Check additional text edits if present
        if (!expectedItem.additionalTextEdits().isEmpty()) {
            Assert.assertTrue(actualItem.additionalTextEdits().size() == expectedItem.additionalTextEdits().size(),
                    "Additional text edits size size should mactch");
            for (int i = 0; i < actualItem.additionalTextEdits().size(); i++) {
                TextEdit actualEdit = actualItem.additionalTextEdits().get(i);
                TextEdit expectedEdit = expectedItem.additionalTextEdits().get(i);
                Assert.assertTrue(edit.text().equals(expectedEdit.text()) 
                        && actualEdit.range().startOffset() == expectedEdit.range().startOffset() 
                        && actualEdit.range().length() == expectedEdit.range().length());
            }
        }
    }
```
