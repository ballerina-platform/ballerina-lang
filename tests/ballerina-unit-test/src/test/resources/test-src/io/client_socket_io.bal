import ballerina/io;

function openSocketConnection(string host, int port) returns io:Socket|error {
    io:Socket s = new;
    check s.connect(host, port);
    return s;
}

function openSocketConnectionWithProps(string host, int port, int localPort) returns io:Socket|error {
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
    return byteChannel.write(content, 0);
}

function read(io:Socket socket, int size) returns (byte[], int)|error {
    io:ReadableByteChannel byteChannel = socket.readableChannel;
    return byteChannel.read(size);
}

function readShutdown(io:Socket s) returns error? {
    return s.shutdownInput();
}

function writeShutDown(io:Socket s) returns error? {
    return s.shutdownOutput();
}

function bindSocketForSamePort(int localPort) returns error? {
    io:Socket client1 = new;
    check client1.bindAddress(localPort);
    io:Socket client2 = new;
    var bindResult = client2.bindAddress(localPort);
    if (bindResult is error) {
        check client1.close();
        return bindResult;
    } else {
        return ();
    }
}

function readRecord(io:Socket socket) returns string[]|error {
    io:ReadableByteChannel byteChannel = socket.readableChannel;
    io:ReadableCharacterChannel characterChannel = new(byteChannel, "UTF-8");
    io:ReadableTextRecordChannel rChannel = new io:ReadableTextRecordChannel(characterChannel, rs = "\r\n", fs = ",");
    if (rChannel.hasNext()){
        return rChannel.getNext();
    } else {
        error err = error("No records found");
        return err;
    }
}
