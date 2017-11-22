package org.ballerinalang.langserver.launchers.socket.server;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Launcher class for language server
 * */
public class LanguageServerSocketLauncher {
    public static void main(String[] args) throws Exception {
        String port = args[1];
        String host = args[0];
        try {
            Socket socket = new Socket(host, Integer.parseInt(port));

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
            Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(languageServer, in, out);

            LanguageClient client = launcher.getRemoteProxy();
            languageServer.connect(client);
            launcher.startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
