package org.ballerinalang.composer.service.workspace.langserver.ws;

import org.ballerinalang.composer.service.workspace.langserver.ThinLangServer;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageProducer;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

import javax.websocket.CloseReason;
import javax.websocket.Session;

public class WSMessageProducer implements MessageProducer {

    private final MessageJsonHandler jsonHandler;
    private final ThinLangServer thinLangServer;
    private boolean keepRunning = false;

    public WSMessageProducer(ThinLangServer thinLangServer, MessageJsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
        this.thinLangServer = thinLangServer;
    }

    @Override
    public void listen(MessageConsumer messageConsumer) {
        this.keepRunning = true;
        this.thinLangServer.addSocketCloseListner((closeReason, session) -> {
            if (this.thinLangServer.getSessions().isEmpty()) {
                this.keepRunning = false;
            }
        });
        this.thinLangServer.addTextMessageListener((message, session) -> {
            String json = message.split("Content-Length:\\s[\\d]*\\s\\n")[1];
            messageConsumer.consume(this.jsonHandler.parseMessage(json));
        });
    }
}
