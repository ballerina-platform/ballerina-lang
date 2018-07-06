import ballerina/io;

string returnValue;

//function startServerSocket(int port, string welcomeMsg) {
//    io:ServerSocket server = new();
//    check server.bindAddress(port);
//    io:Socket soc = check server.accept();
//    io:println("Client socket accepted!!!");
//    io:ByteChannel ch = soc.channel;
//    byte[] c1 = welcomeMsg.toByteArray("utf-8");
//    _ = check ch.write(c1, 0);
//    io:CharacterChannel? characterChannel = new io:CharacterChannel(ch, "utf-8");
//    var result = characterChannel.read(50);
//    match result {
//        string characters => {
//            returnValue = untaint characters;
//        }
//        error err => {
//            io:println(err.message);
//        }
//        () => {
//            io:println("Character channel not initialized properly");
//        }
//    }
//    check soc.close();
//    io:println("Client done.");
//    check server.close();
//}

function getResultValue() returns string {
    return returnValue;
}

function readAllCharacters(io:CharacterChannel characterChannel) returns string|error? {
    int fixedSize = 50;
    boolean isDone = false;
    string result;
    while (!isDone) {
        string value = check readCharacters(fixedSize, characterChannel);
        if (lengthof value == 0){
            isDone = true;
        } else {
            result = result + value;
        }
    }
    return result;
}

function readCharacters(int numberOfCharacters, io:CharacterChannel characterChannel) returns string|error {
    var result = characterChannel.read(numberOfCharacters);
    match result {
        string characters => {
            return characters;
        }
        error err => {
            return err;
        }
    }
}

function startServerSocket(int port, string welcomeMsg) {
    io:ServerSocket server = new();
    io:println("Server started");
    check server.bindAddress(port);
    match server.accept() {
        io:Socket s => {
            io:println("Client socket accepted!!!");
            io:println(s.remotePort);
            io:ByteChannel ch = s.channel;
            byte[] c1 = welcomeMsg.toByteArray("utf-8");
            match ch.write(c1, 0) {
                int i => {
                    io:println("No of bytes written: ", i);
                }
                error e2 => {
                    io:println("Channel write error: ", e2.message);
                }
            }
            io:CharacterChannel? characterChannel1 = new io:CharacterChannel(ch, "utf-8");
            match characterChannel1 {
                io:CharacterChannel characterChannel => {
                    match readAllCharacters(characterChannel) {
                        string str => {
                            returnValue = untaint str;
                        }
                        error err => {
                            io:println("Error in read: ", err.message);
                        }
                        () => {
                            io:println("Empty return from channel.");
                        }
                    }
                    match characterChannel.close() {
                        error e1 => {
                            io:println("CharacterChannel close error: ", e1.message);
                        }
                        () => {
                            io:println("Connection closed successfully.");
                        }
                    }
                }
                () => {

                }
            }
            check s.close();
            io:println("Client done.");
        }
        error e10 => {
            io:println("Socket accept error: " , e10.message);
        }
    }
}
