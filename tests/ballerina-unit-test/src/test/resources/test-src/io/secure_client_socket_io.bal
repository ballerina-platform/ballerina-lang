import ballerina/io;

io:Socket socket;

function openSocketConnection(string host, int port, io:SocketProperties prop) {
    var result = io:openSecureSocket(host, port, prop);
    match result {
        io:Socket s => {
            socket = untaint s;
        }
        error err => {
            throw err;
        }
    }
}

function closeSocket() {
    error? err = socket.close();
}

function write(byte[] content) returns int|error {
    io:WritableByteChannel byteChannel = socket.writableChannel;
    var result = byteChannel.write(content, 0);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        error err => {
            return err;
        }
    }
}

function read(int size) returns (byte[], int)|error {
    io:ReadableByteChannel byteChannel = socket.readableChannel;
    var result = byteChannel.read(size);
    match result {
        (byte[], int) content => {
            var (bytes, numberOfBytes) = content;
            return (bytes, numberOfBytes);
        }
        error err => {
            return err;
        }
    }
}
