package io.ballerina.plugins.idea.extensions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorMouseListener;
import io.ballerina.plugins.idea.extensions.client.BallerinaRequestManager;
import io.ballerina.plugins.idea.extensions.editoreventmanager.BallerinaEditorEventManager;
import io.ballerina.plugins.idea.extensions.server.BallerinaExtendedLangServer;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.wso2.lsp4intellij.client.ClientContext;
import org.wso2.lsp4intellij.client.DefaultLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.ServerOptions;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.DefaultRequestManager;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.RequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.editor.listeners.EditorMouseMotionListenerImpl;
import org.wso2.lsp4intellij.extensions.LSPExtensionManager;

/**
 * Contains extended LSP components which serves  ballerina language server related specific capabilities.
 */
public class BallerinaLSPExtensionManager implements LSPExtensionManager {

    @Override
    public <T extends DefaultRequestManager> T getExtendedRequestManagerFor(LanguageServerWrapper wrapper,
            LanguageServer server, LanguageClient client, ServerCapabilities serverCapabilities) {
        return (T) new BallerinaRequestManager(wrapper, server, client, serverCapabilities);
    }

    @Override
    public <T extends EditorEventManager> T getExtendedEditorEventManagerFor(Editor editor,
            DocumentListener documentListener, EditorMouseListener mouseListener,
            EditorMouseMotionListenerImpl mouseMotionListener, RequestManager requestManager,
            ServerOptions serverOptions, LanguageServerWrapper wrapper) {
        return (T) new BallerinaEditorEventManager(editor, documentListener, mouseListener, mouseMotionListener,
                requestManager, serverOptions, wrapper);
    }

    @Override
    public Class<? extends LanguageServer> getExtendedServerInterface() {
        return BallerinaExtendedLangServer.class;
    }

    @Override
    public LanguageClient getExtendedClientFor(ClientContext context) {
        return new DefaultLanguageClient(context);
    }
}
