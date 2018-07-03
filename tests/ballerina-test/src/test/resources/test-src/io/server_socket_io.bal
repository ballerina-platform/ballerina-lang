import ballerina/io;


function startServerSocket (int port, string welcomeMsg) {
    io:ServerSocket server = new();
    check server.bindAddress(port);
    io:Socket soc = check server.accept();
    io:println("Client socket accepted!!!");
    check soc.close();
    check server.close();
}
