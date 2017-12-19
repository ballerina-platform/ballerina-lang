package org.ballerinalang.composer.service.workspace.langserver.ws;

import org.ballerinalang.composer.service.workspace.langserver.ThinLangServer;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WSMessageConsumer implements MessageConsumer {

    String encoding = StandardCharsets.UTF_8.name();
    Object outputLock = new Object();
    private final MessageJsonHandler jsonHandler;
    private final ThinLangServer thinLangServer;

    public WSMessageConsumer(ThinLangServer thinLangServer, MessageJsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
        this.thinLangServer = thinLangServer;
    }

    @Override
    public void consume(Message message) {
        if (message.getJsonrpc() == null) {
            message.setJsonrpc("2.0");
        }
        String content = this.jsonHandler.serialize(message);
        String header = this.getHeader(content.length());
        Object var7 = this.outputLock;
        synchronized(this.outputLock) {
            this.thinLangServer.sendMessageToAll(header + content);
        }
    }

    protected String getHeader(int contentLength) {
        StringBuilder headerBuilder = new StringBuilder();
        this.appendHeader(headerBuilder, "Content-Length", contentLength).append("\r\n");
        if (!StandardCharsets.UTF_8.name().equals(this.encoding)) {
            this.appendHeader(headerBuilder, "Content-Type", "application/json");
            headerBuilder.append("; charset=").append(this.encoding).append("\r\n");
        }

        headerBuilder.append("\r\n");
        return headerBuilder.toString();
    }

    protected StringBuilder appendHeader(StringBuilder builder, String name, Object value) {
        return builder.append(name).append(": ").append(value);
    }
}
