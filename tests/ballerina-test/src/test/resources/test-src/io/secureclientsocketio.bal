import ballerina.io;

io:Socket socket;

function openSocketConnection (string host, int port, io:SocketProperties prop) {
    socket, _ = io:openSecureSocket(host, port, prop);
}

function closeSocket () {
    _ = socket.closeSocket();
}

function write (blob content, int size) (int) {
    int numberOfBytesWritten;
    io:ByteChannel channel = socket.channel;
    numberOfBytesWritten, _ = channel.write(content, size, 0);
    return numberOfBytesWritten;
}

function read (int size) (blob, int) {
    io:ByteChannel channel = socket.channel;
    blob readContent;
    int readSize;
    readContent, readSize, _ = channel.read(size, size, 0);
    return readContent, readSize;
}
