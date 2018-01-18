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

    public WSRPCMessageProducer(BallerinaLangServerService wsLangServer, MessageJsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
        this.wsLangServer = wsLangServer;
    }

    @Override
    public void listen(MessageConsumer messageConsumer) {
        this.wsLangServer.addTextMessageListener((message, session) -> {
            // TODO Fix the logic for message which does not send Content-Length
            String json = message.split("Content-Length:\\s[\\d]*\\s\\n")[1];
            messageConsumer.consume(this.jsonHandler.parseMessage(json));
        });
    }
}
