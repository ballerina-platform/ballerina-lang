import ballerina/io;

string returnValue;

function startServerSocket(int port, string welcomeMsg) {
    io:ServerSocket server = new();
    check server.bindAddress(port);
    io:Socket soc = check server.accept();
    io:println("Client socket accepted!!!");
    io:ByteChannel ch = soc.channel;
    byte[] c1 = welcomeMsg.toByteArray("utf-8");
    _ = check ch.write(c1, 0);
    io:CharacterChannel? characterChannel = new io:CharacterChannel(ch, "utf-8");
    var result = characterChannel.read(50);
    match result {
        string characters => {
            returnValue = untaint characters;
        }
        error err => {
            io:println(err.message);
        }
        () => {
            io:println("Character channel not initialized properly");
        }
    }
    check soc.close();
    io:println("Client done.");
    check server.close();
}

function getResultValue() returns string {
    return returnValue;
}
