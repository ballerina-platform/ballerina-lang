# Writing Extended Services for Language Server
- <a href="#WhatIsAService">What is a Service?</a>
- <a href="#ComponentsOfAService">Components of a Service</a>
    - <a href="#ServiceImplementation">Service Implementation</a>
    - <a href="#ServerCapability">Server Capability Registration</a>
    - <a href="#ClientCapability">Client Capability Registration</a>

<a name="WhatIsAService"></a>
## What is a Service?
The Language Server Protocol(LSP) defines various namespaces for LSP operations. For example, consider the [auto-ccompletion](https://microsoft.github.io/language-server-protocol/specifications/specification-3-16/#textDocument_completion) capability in the LSP. The auto-completion operation's JSON-RPC method is defined as `textDocument/completion`. Here, the `textDocument` prefix defines a namespace for the operation. Similarly the workspace's [watched file did change](https://microsoft.github.io/language-server-protocol/specifications/specification-3-16/#workspace_didChangeWatchedFiles) event in LSP is defined as `workspace/didChangeWatchedFiles` where the `workspace` denotes the namespace.

By default, the language server protocol defines two services as the `WorkspaceService` and the `TextDocumentService`.

With this extended service capability we are are going to introduce an extensible API to allow the third parties to bind custom services and extend the Language Server Protocol's default behaviour.

<a name="ComponentsOfAService"></a>
## Components of a Service
In order to register a new service, there are three must have components. 
1. Service Implementation
2. Server Capability Registration
3. Client Capability Registration

<a name="ServiceImplementation"></a>
### Service Implementation
The service implementation defines the service namespace and the service operation. The service is dynamically picked and registered at the language server startup. The services will be registered with Java SPI implementation. In order to register a new service, following approach should be considered and adding the following annotation is mandatory.
```Java
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
```

Eg: 
```Java
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("exampleService")
public class CustomService implements ExtendedLanguageServerService {
    @JsonRequest
    public CompletableFuture<CustomResponse> syntaxApiCalls(CustomRequestParams request) {
        // This is a custom request implementation denoted by the @JsonRequest annotation
    }

    @JsonNotification
    public void syntaxApiCalls(CustomNotificationParams request) {
        // This is a custom notification implementation denoted by the @JsonNotification annotation
    }
}
```

As per the above snippet we can register the service and get it working without any other configuration associated with the service. 
The Ballerina Lannguage Server exposes context information for the use of the extension APIs to be used. Depending on the requirement. the extension developer can use different context information as follows,

#### Default/ without overriding `init` method
This will ensure the default behaviour without exposing context information for the extension

#### Initialization option-1
```Java
default void init(LanguageServer langServer,
                    WorkspaceManager workspaceManager) {
    // access and store the parameters
}
```
Exposes the language server instance and the default workspace manager.

#### Initialization option-2
```Java
default void init(LanguageServer langServer,
                    WorkspaceManager workspaceManager,
                    LanguageServerContext serverContext) {
    // access and store the parameters
}
```
Additionally exposes the `LanguageServerContext`

#### Initialization option-3
```Java
default void init(LanguageServer langServer,
                      WorkspaceManagerProxy workspaceManagerProxy,
                      LanguageServerContext serverContext) {
    // access and store the parameters
}
```
Instead of the `WorkspaceManager`, this exposes the `WorkspaceManagerProxy`. The `WorkspaceManagerProxy` will expose both the default `WorkspaceManager` and the `expr` file scheme based workapace manager.

<a name="ServerCapability"></a>
### Server Capability Registration
Once the service implementation is registered, it is important to register the server capabilities. The server capability registration allows the server to notify the clients on the operation capabilities exposed with a particular custom service.

The speccific server capabilities data model represents the signature of the server capabilities of the particular service implementation. The data types of the specific operation related fields should be serializable.

```Java
public class CustomServiceServerCapabilities extends BallerinaServerCapability {
    private boolean supportCustomOperation;
    // getters and setters

    public CustomServiceClientCapabilities() {
        super(nameOfService)
    }
}
```

The sever capability setter specifies the server capability setting approach. The extension service can define how the capabilities are set by overriding the `build` method. There is another `build` method, which will take a json object as the argument, and that will not be used when creating the server extensions but will be used when generating the client extensions from java.

```Java
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter")
public class CustomServiceServerCapabilitySetter
        extends BallerinaServerCapabilitySetter<CustomServiceServerCapabilities> {
    @Override
    public Optional<CustomServiceServerCapabilities> build() {
        CustomServiceServerCapabilities capabilities = new CustomServiceServerCapabilities();
        capabilities.setSupportCustomOperation(true);
        
        return Optional.of(capabilities);
    }
    
    public String getCapabilityName() {
        return nameOfService;
    }

    @Override
    public Class<CustomServiceServerCapabilities> getCapability() {
        return CustomServiceServerCapabilities.class;
    }
}
```

<a name="ClientCapability"></a>
### Client Capability Registration
Once the service implementation is registered, it is important to register the client capabilities. The client capability registration allows the client to notify the server on the operation capabilities supported by the client.

The speccific client capabilities data model represents the signature of the client capabilities of the particular service implementation. The data types of the specific operation related fields should be serializable.

```Java
public class CustomServiceClientCapabilities extends BallerinaClientCapability {    
    private boolean supportCustomOperation;

    // getters and setters
    public CustomServiceClientCapabilities() {
        super(nameOfService);
    }
}
```

The client capability setter specifies the client capability setting approach. For the server extensions, there is no need to override the `build` methods, since the default behavior would be enough.

```Java
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter")
public class CustomServiceClientCapabilitySetter
        extends BallerinaClientCapabilitySetter<CustomServiceClientCapabilities> {
    
    public String getCapabilityName() {
        return nameOfService;
    }

    @Override
    public Class<CustomServiceClientCapabilities> getCapability() {
        return CustomServiceClientCapabilities.class;
    }
}
```

__Note:__
> It is important to note that the `nameOfService` which has been used in four places above, should be the same string value since the particular string value is used to correlate the capabilites with the services.
