package org.ballerinalang.vscode.server;

import org.ballerinalang.composer.server.core.Server;
import org.ballerinalang.composer.server.core.ServerConfig;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Launcher for ballerina language server.
 */
public class Main {
    public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException {
        String port = args[0];
        String parserPort = args[1];
        
        Socket socket = new Socket("localhost", Integer.parseInt(port));
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        
        BallerinaLanguageServer server = new BallerinaLanguageServer();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);
        
        LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);
        launcher.startListening();

        ServerConfig config = new ServerConfig();
        config.setPort(Integer.parseInt(parserPort));
        config.setHost("127.0.0.1");
        Server parser = new Server(config);
        parser.start();
        PrintStream sout = System.out;
        sout.println("Parser started successfully");
    }
}
