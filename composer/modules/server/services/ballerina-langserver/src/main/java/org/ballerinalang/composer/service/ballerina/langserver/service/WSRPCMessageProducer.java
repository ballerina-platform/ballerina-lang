package org.ballerinalang.composer.service.ballerina.langserver.service;

import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;

/**
 * WS Based message producer for language JSON RPC Server.
 */
public class WSRPCMessageProducer implements MessageProducer {

    private final MessageJsonHandler jsonHandler;
    private final BallerinaLangServerService wsLangServer;
    private static final String CONTENT_LENGTH = "Content-Length:\\s[\\d]*\\s\\n";

    public WSRPCMessageProducer(BallerinaLangServerService wsLangServer, MessageJsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
        this.wsLangServer = wsLangServer;
    }

    @Override
    public void listen(MessageConsumer messageConsumer) {
        this.wsLangServer.addTextMessageListener((message, session) -> {
            String json = message;
            // if content length prefix is present, stripe it out
            if (message.startsWith("Content-Length")) {
                json = message.split(CONTENT_LENGTH)[1];
            }
            messageConsumer.consume(this.jsonHandler.parseMessage(json));
        });
    }
}
