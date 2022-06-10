# Document event sync publisher subscriber

## Overview

This document contains information about the implementation of a notification system inside the language server using the 
PubSub architecture. Goal is to allow communication between language server and extensions synchronously.

Following diagram shows how this has been implemented. 

![alt text](../images/language-server/001-DocumentEventPubSub-class-diagram.png)

- Event Subscribers and Event Publishers get loaded using JavaSPI and get subscribed to publishers when a document 
opens or changes.
- Event Publishers and Subscribers have an Event kind and a subscriber can only subscribe to a single publisher at once.
If we need a subscriber to subscribe for two publishers, then we have to create two different subscribers and subscribe
separately.
- When an Event Publisher publishes, the `onEvent()` method is getting triggered for each subscriber that has been
subscribed to the publisher.
- We can override `publish()` and `onEvent()` methods to control the event synchronization.
  

## How to Use

- **Event Publisher** is an internal interface and cannot be accessed publicly by extension developers. The server 
decides which events to be delegated to a particular publisher. In the `publish()` method a debouncing mechanism has 
implemented for the **Project Update Event Publisher**.
- **Event Subscriber** is an interface which can be accessed publicly by extension developers and get notified when 
subscribed to a publisher.
- If the extension uses different file schemes, users have to handle that from extension itself.
  (ex: expression file scheme)

## Example

### 1. Implementing a Subscriber

We can implement `EventSubscriber` interface as follows.

```java
@JavaSPIService("org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber")
public class CommandRegisterSubscriber implements EventSubscriber {
    public static final String NAME = "Command register subscriber";

    @Override
    public EventKind eventKind() {
        return EventKind.PROJECT_UPDATE;
    }
    
    @Override
    public void onEvent(ExtendedLanguageClient client, DocumentServiceContext context,
                        LanguageServerContext languageServerContext) {
        checkAndRegisterCommands(context);
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    public static synchronized void checkAndRegisterCommands(DocumentServiceContext context) {
        LanguageServerContext serverContext = context.languageServercontext();
        LSClientLogger clientLogger = LSClientLogger.getInstance(serverContext);

        ServerCapabilities serverCapabilities = serverContext.get(ServerCapabilities.class);
        if (serverCapabilities.getExecuteCommandProvider() == null) {
            clientLogger.logTrace("Not registering commands: server isn't a execute commands provider");
            return;
        }

        if (!isDynamicCommandRegistrationSupported(serverContext)) {
            clientLogger.logTrace("Not registering commands: client doesn't support dynamic commands registration");
            return;
        }
        context.workspace().waitAndGetPackageCompilation(context.filePath())
                .ifPresent(pkgCompilation 
                        -> compileAndRegisterCommands(serverContext, clientLogger, serverCapabilities, pkgCompilation));
    }
}
```
### 2. Implementing a Publisher

`EventPublishers` can be implemented extending `AbstractEventPublisher`.

```java
@JavaSPIService("org.ballerinalang.langserver.eventsync.EventPublisher")
public class ProjectUpdateEventPublisher extends AbstractEventPublisher {
    public static final String NAME = "Project update event publisher";
    private CompletableFuture<Boolean> latestScheduled = null;
    private static final long DIAGNOSTIC_DELAY = 1;
    
    @Override
    public EventKind getKind() {
        return EventKind.PROJECT_UPDATE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void publish(ExtendedLanguageClient client, LanguageServerContext serverContext,
                        DocumentServiceContext context) {
        if (latestScheduled != null && !latestScheduled.isDone()) {
            latestScheduled.completeExceptionally(new Throwable("Cancelled project update event publisher"));
        }

        Executor delayedExecutor = CompletableFuture.delayedExecutor(DIAGNOSTIC_DELAY, TimeUnit.SECONDS);
        CompletableFuture<Boolean> scheduledFuture = CompletableFuture.supplyAsync(() -> true, delayedExecutor);
        latestScheduled = scheduledFuture;
        scheduledFuture.thenAcceptAsync(aBoolean -> 
                subscribers.forEach(subscriber -> subscriber.onEvent(client, context, serverContext)));
    }
}
```