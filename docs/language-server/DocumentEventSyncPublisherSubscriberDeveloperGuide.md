# Document Event Sync Publisher Subscriber - Developer Guide

This documentation is intended for developers who are planning to write their own `Subscriber` implementations.
Read about the design of document event sync publisher subscriber from 
[here](./DocumentEventSyncPublisherSubscriberDesign.md) .

## Example

Let us consider a scenario where we need to get event sync notifications from language server to a language server
extension (ls extension) `trigger-serivce`.

### Step 1 - Creating an `EventSubscriber`

To get notifications from a `EventPublisher` from language server, first we need to create a Subscriber by implementing 
the `EventSubscriber` interface. This is an example of an `EventSubscriber` for ls extension `trigger-service`.

- You need to add the JavaSPI annotation since we load the `EventSubscriber`s using JavaSPI.
- In this example I will set the `EventKind` as `EventKind.PROJECT_UPDATE`, which is the kind of the publisher I want 
to subscribe my `EventSubscriber` to.
- `onEvent()` method gets triggered when the publisher publishes and implement it accordingly. I have kept it blank for 
this example.

```java
@JavaSPIService("org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber")
public class TriggerServiceSubscriber implements EventSubscriber {
    public static final String NAME = "Trigger service subscriber";

    @Override
    public EventKind eventKind() {
        return EventKind.PROJECT_UPDATE;
    }

    @Override
    public void onEvent(ExtendedLanguageClient client,
                        DocumentServiceContext context,
                        LanguageServerContext languageServerContext) {
    }

    @Override
    public String getName() {
        return NAME;
    }
}
```
See [`CommandRegisterSubscriber`](../../language-server/modules/langserver-core/src/main/java/org/ballerinalang/langserver/eventsync/subscribers/CommandRegisterSubscriber.java) 
for an example

### Step 2 - Adding Services to `META-INF`

- After creating the `TriggerServiceSubscriber` you need to create a file with the name 
`org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber` inside `resources/META-INF/services` file inside 
the `trigger-service` ls extension. 
- Then add the class path inside that file to make it readable from Service Loader as follows.

```text
io.ballerina.trigger.TriggerServiceSubscriber
```

### Step 3 - Testing

#### 1. Manual Testing

- We use `EventSyncPubSubHolder` for loading, subscribing and logging information about`EventPublisher`s and
`EventSubscriber`s. 
- You will get the following log message in VSCode if you have created and subscribed to the specific publisher 
correctly as in the example. 
```
Trigger service subscriber subscribed to Project update event publisher
```

#### 2. Unit Testing

- Unit tests are written inside `EventSyncPubSubTest`. 
