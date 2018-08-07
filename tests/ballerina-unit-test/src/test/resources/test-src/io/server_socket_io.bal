import ballerina/io;

string returnValue;

function getResultValue() returns string {
    return returnValue;
}

function readAllCharacters(io:CharacterChannel characterChannel) returns string|error? {
    int fixedSize = 50;
    boolean isDone = false;
    string result;
    while (!isDone) {
        match readCharacters(fixedSize, characterChannel) {
            string value => {
                result = result + value;
            }
            error err => {
                if (err.message == "io.EOF"){
                    isDone = true;
                } else {
                    return err;
                }
            }
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
            io:ByteChannel ch = s.byteChannel;
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
