import ballerina.io;

io:Socket socket;

function openSocketConnection (string host, int port, io:SocketProperties prop) {
    socket = io:openSecureSocket(host, port, prop);
}

function closeSocket () {
    socket.closeSocket();
}

function write (blob content) (int) {
    io:ByteChannel channel = socket.channel;
    return channel.writeBytes(content, 0);
}

function read (int size) (blob, int) {
    io:ByteChannel channel = socket.channel;
    blob readContent;
    int readSize;
    readContent, readSize = channel.readBytes(size);
    return readContent, readSize;
}
