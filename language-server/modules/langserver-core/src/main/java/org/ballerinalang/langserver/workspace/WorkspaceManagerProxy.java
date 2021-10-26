package org.ballerinalang.langserver.workspace;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;

public interface WorkspaceManagerProxy {
    WorkspaceManager get();
    
    WorkspaceManager get(String fileUri);
    
    void didOpen(DidOpenTextDocumentParams params) throws WorkspaceDocumentException;
    
    void didChange(DidChangeTextDocumentParams params) throws WorkspaceDocumentException;
    
    void didClose(DidCloseTextDocumentParams params) throws WorkspaceDocumentException;
}
