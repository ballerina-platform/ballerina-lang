package org.ballerinalang.composer.service.workspace.langserver;

import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.ballerinalang.composer.service.workspace.langserver.dto.RequestMessage;
import org.ballerinalang.composer.service.workspace.langserver.dto.TextDocumentPositionParams;

/**
 * Message utils for manipulating the Message objects
 */
public class MessageUtil {
    private static final String METHOD_COMPLETION = "textDocument/completion";

    /**
     * Get a new request message from the content
     * @param balContent text content
     * @param position position of the cursor
     * @param id message id
     * @return {@link RequestMessage}
     */
    public static RequestMessage getRequestMessage(String balContent, Position position, String id) {
        RequestMessage requestMessage = new RequestMessage();
        TextDocumentPositionParams textDocumentPositionParams = new TextDocumentPositionParams();

        textDocumentPositionParams.setFilePath("/");
        textDocumentPositionParams.setFileName("untitled");
        textDocumentPositionParams.setPackageName(".");
        textDocumentPositionParams.setPosition(position);
        textDocumentPositionParams.setText(balContent);

        requestMessage.setId(id);
        requestMessage.setMethod(METHOD_COMPLETION);
        requestMessage.setParams(textDocumentPositionParams);
        requestMessage.setJsonrpc("2.0");

        return requestMessage;
    }
}
