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
    List<Class<T>> getAttachmentPoints();
}

```
1. `name()`: The name of the completion provider.

2. `getCompletions()`: The method that returns the list of completion items for a given completion context.

3. `getAttachmentPoints()`: The method that returns the list of classes of syntax nodes that the completion provider is attached to. This is used to filter out the completion providers that are not relevant to the current completion context.

#### `AbstractCompletionProvider` 

The `AbstractCompletionProvider` defined in `io.ballerina.projects.plugins.completion` package is an abstract class that implements the `CompletionProvider` interface. This class has the `getAttachmentPoints()` method implemented. A compiler plugin developer can extend this class to develop a completion provider. 

```java
/**
 * Interface for completion item providers.
 *
 * @param <T> Provider's node type
 * @since 2201.7.0
 */
public abstract class AbstractCompletionProvider<T extends Node> implements CompletionProvider<T> {

    private final List<Class<T>> attachmentPoints;

    public AbstractCompletionProvider(List<Class<T>> attachmentPoints) {
        this.attachmentPoints = attachmentPoints;
    }

    public AbstractCompletionProvider(Class<T> attachmentPoint) {
        this.attachmentPoints = List.of(attachmentPoint);
    }

    @Override
    public List<Class<T>> getAttachmentPoints() {
        return this.attachmentPoints;
    }
}

```

#### `CompletionItem`

A completion item has several properties. As a compiler plugin developer, you can define the following properties of a completion item:

- `label`: The label of the completion item. This is the text displayed in the list of completion items.

- `insertText`: A string that should be inserted a document when the completion item is selected.

- `priority`: The priority of this item defines the order in which the items are shown to the user. If the priority is high the item will be shown before the item will be grouped at the top of the completion item list.


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

You can write a completion provider extending the `AbstractCompletionProvider` class as follows:

```java
/**
 * An example of a completion provider that adds a resource function to a service declaration.
 *
 * @since 2201.7.0
 */
public class ServiceBodyContextProvider extends AbstractCompletionProvider<ServiceDeclarationNode> {
    public ServiceBodyContextProvider(Class<ServiceDeclarationNode> attachmentPoint) {
        super(attachmentPoint);
    }

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
        pluginContext.addCompletionProvider(new ServiceBodyContextProvider(ServiceDeclarationNode.class));
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

[completions](images/compiler-plugin-completions.png)

Step 4: Unit testing

In unit testing, we have to test list of completion items given the cursor position on a source file.

