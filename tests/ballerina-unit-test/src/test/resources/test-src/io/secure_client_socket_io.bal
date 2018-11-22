import ballerina/io;

io:Socket socket = new;

function openSocketConnection(string host, int port, io:SocketProperties prop) {
    var result = io:openSecureSocket(host, port, prop);
    if (result is io:Socket) {
        socket = untaint result;
    } else {
        panic result;
    }
}

function closeSocket() {
    error? err = socket.close();
}

function write(byte[] content) returns int|error {
    io:WritableByteChannel byteChannel = socket.writableChannel;
    return byteChannel.write(content, 0);
}

function read(int size) returns (byte[], int)|error {
    io:ReadableByteChannel byteChannel = socket.readableChannel;
    return byteChannel.read(size);
}
