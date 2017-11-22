package org.ballerinalang.langserver.launchers.socket.client;

import org.ballerinalang.langserver.client.LanguageClientImpl;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;

public class LanguageSocketClientLauncher {
    public static void main(String[] args) throws Exception {
        LanguageClientImpl client = new LanguageClientImpl();
//        Launcher<LanguageServer> launcher = LSPLauncher.createClientLauncher(client, input, output);
//        launcher.startListening();
    }
}
