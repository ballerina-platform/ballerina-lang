package org.ballerinalang.composer.service.workspace.langserver.ws;

import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;

/**
 * WS Based message producer for language JSON RPC Server.
 */
public class WSMessageProducer implements MessageProducer {

    private final MessageJsonHandler jsonHandler;
    private final WSLangServer wsLangServer;

    public WSMessageProducer(WSLangServer wsLangServer, MessageJsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
        this.wsLangServer = wsLangServer;
    }

    @Override
    public void listen(MessageConsumer messageConsumer) {
        this.wsLangServer.addTextMessageListener((message, session) -> {
            String json = message.split("Content-Length:\\s[\\d]*\\s\\n")[1];
            messageConsumer.consume(this.jsonHandler.parseMessage(json));
        });
    }
}
