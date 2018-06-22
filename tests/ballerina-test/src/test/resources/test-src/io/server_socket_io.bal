import ballerina/io;

io:ServerSocket server;

function startServerSocket (int port, string welcomeMsg) {
    io:ServerSocket s = new();
    check s.bindAddress(port);
    server = s;
    while (true) {
        io:Socket soc = check s.accept();
        _ = start handleSocket(soc, welcomeMsg);
    }
}

function handleSocket(io:Socket s, string welcomeMsg) {
    io:ByteChannel ch = s.channel;
    blob c1 = welcomeMsg.toBlob("utf-8");
    _ = check ch.write(c1, 0);
    check s.close();
}

function closeServerSocket() {
    check server.close();
}
