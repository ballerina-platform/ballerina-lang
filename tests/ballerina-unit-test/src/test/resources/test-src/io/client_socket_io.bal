import ballerina/io;

function openSocketConnection(string host, int port) returns io:Socket {
    io:Socket s = new;
    check s.connect(host, port);
    return s;
}

function openSocketConnectionWithProps(string host, int port, int localPort) returns io:Socket {
    io:Socket s = new;
    check s.bindAddress(localPort);
    check s.connect(host, port);
    return s;
}

function closeSocket(io:Socket socket) {
    error? err = socket.close();
}

function write(io:Socket socket, byte[] content) returns int|error {
    io:WritableByteChannel byteChannel = socket.writableChannel;
    var result = byteChannel.write(content, 0);
    match result {
        int numberOfBytesWritten => {
            io:println("Number of byte written to server: ", numberOfBytesWritten);
            return numberOfBytesWritten;
        }
        error err => {
            return err;
        }
    }
}

function read(io:Socket socket, int size) returns (byte[], int)|error {
    io:ReadableByteChannel byteChannel = socket.readableChannel;
    var result = byteChannel.read(size);
    match result {
        (byte[], int) content => {
            var (bytes, numberOfBytes) = content;
            io:println("Number of byte read from server: ", numberOfBytes);
            return (bytes, numberOfBytes);
        }
        error err => {
            return err;
        }
    }
}

function readShutdown(io:Socket s) {
    check s.shutdownInput();
}

function writeShutDown(io:Socket s) {
    check s.shutdownOutput();
}

function bindSocketForSamePort(int localPort) returns error? {
    io:Socket client1 = new;
    check client1.bindAddress(localPort);
    io:Socket client2 = new;
    match client2.bindAddress(localPort) {
        error e => {
            check client1.close();
            return e;
        }
        () => {
            return ();
        }
    }
}

function readRecord(io:Socket socket) returns string[]|error {
    io:ReadableByteChannel byteChannel = socket.readableChannel;
    io:ReadableCharacterChannel characterChannel = new(byteChannel, "UTF-8");
    io:ReadableTextRecordChannel rChannel = new io:ReadableTextRecordChannel(characterChannel, rs = "\r\n", fs = ",");
    if (rChannel.hasNext()){
        var records = rChannel.getNext();
        match records {
            error e => {
                return e;
            }
            string[] fields => {
                return fields;
            }
        }
    } else {
        return { message: "No records found" };
    }
}
