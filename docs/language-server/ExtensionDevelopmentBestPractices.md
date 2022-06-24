# Extension Development Best Practices

## What is Language Server Extension

The language server protocol contains various language features and the ballerina's LSP implementation exposes a set of
extension points where third parties can write their own extensions for these language features and enrich the user
experience. At the moment following extension points are provided.

1. [Extended Language Server Services](WritingExtendedServices.md)
2. [Compiler Plugin Code Actions](CompilerPluginCodeActions.md)
3. Code Action Extension (Java SPI)
4. Completion Extension (Java SPI)
5. ~~Formattin Extenstion (Java SPI)~~
6. ~~Diagnostic Extension (Java SPI)~~

__NOTE__: Formatting and Diagnostic extensions are not used currently.

In order to compose an extension, the developer should have an understanding of the underlying supporting APIs which
being used heavily in the existing implementations. Following are the main APIs being used in the implementation.

1. Project API
2. Syntax Tree API
3. Semantic API
4. Language Server Context APIs
5. Language Server Workspace Manager APIs

## API Usage Dos and Don'ts

### WorkspaceManager - Recommended Usage

```
When to Use
-----------
1. When the context APIs are not enough for accessing the projet related data.
2. Use the context APIs in the first place and use the WorkspaceManager rarely
```

- The `WorkspaceManager` API allows the extension developers a central and controlled access to the current project and
  associated compiler APIs
- Access the project specific information
- Need to provide the path as an argument for most of the APIs
- No need to worry about synchronization of the source.
- Up to date information is served
- All the project related details can be accessed

### Context APIs

```
When to Use
-----------
1. Always use the context APIs.
2. Most of the semantic and syntactic information are derived through the SyntaxTree and SemanticModel instances.
3. Context APIs expose these details
```

- Each and every extended operation contains a context API.

eg: `CodeActionContext`

- Provides a thin controlled layer to access the workspace manager services specific to the context/ feature.
- Before going for an own implementation of a certain behaviour, check whether there is a context API to facilitate it (
  Because most of the time there is one).

### Semantic API

```
When to Use
-----------
1. When there is a need for accessing the semantic information of the project.
2. If the context APIs contains already calculated data, reuse them for better performance

When not to Use
---------------
1. If the particular information can be captured with the syntax information alone (Using the syntax tree APIs), DO NOT use this API since this API is slower than the syntax tree API.
2. If it is necessary to use this API and a wrapper API is available via the context APIs.

```

- Contains all the utility APIs to access the semantic information in your ballerina project.
- Symbol related information can be captured with the available APIs

### Syntax Tree API

```
When to Use
-----------
1. When the required information/ processing can be done considering only the syntax information.
2. Traversing capturing the scopes
3. Capture the syntax node information such as location, leading and trailing whitespaces, accessing the source code
```

- Syntax APIs are provided to capture the syntax information

### Project API

```
When to Use
- DON'T USE
```

- The project APIs allow you to build a project and access various project details.
- These APIs are heavy APIs, hence strictly adhere to not using them
- Can have a major impact for performance
- Your source content will not be synchronized with the new projects you get with the project APIs
- You have to manually synchronize the syntax changes with the project if you use the project APIs.
- Hence **Don't Use the API**

## Important tips for various Usages of APIs

**1. How do we check for the node is of a specific kind**  
**Answer**: _Use the `SyntaxKind` to check and avoid the use of **InstanceOf** checks_

**2.How to check a symbol is of a specific kind of Symbol**  
**Answer**: _Use the `SymbolKind` and `TypeKind` for the check_

**3. How to get the children of a Syntax node**  
**Answer:** _Use the APIs provided in each of the node. For example the `FunctionDefinition` contains an API
called `qualifierList` to get the qualifiers in the function. Try to avoid the usage of `children` API since it is too
generic and includes whitesapces as well_

**4. How to access the position details**  
**Answer:** _Use either the offset or line-column approach. In either case, stick to one usage without mixing them both.
Better option would be to use the offset approach_

**5. How to Access the Source code from a syntax node**  
**Answer:** _Use the `toSourceCode` API. **NEVER** use the `toString` API_

**6. How to traverse the Syntax Tree**  
**Answer:** _Best and recomended optionis to use a node visitor. The `NodeVisitor` facilitates this. Each `visit` method
will be there for each node and will return `void`. If you need to capture a value with the visit and return a value,
use `NodeTransformer` instead_