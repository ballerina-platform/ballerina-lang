import ballerina/io;

io:Socket socket;

function openSocketConnection (string host, int port, io:SocketProperties prop) {
    var result = io:openSecureSocket(host, port, prop);
    match result {
        io:Socket s => {
            socket = s;
        }
        io:IOError err => {
            throw err;
        }
    }
}

function closeSocket () {
    io:IOError err = socket.closeSocket();
}

function write (blob content) returns (int | io:IOError) {
    io:ByteChannel channel = socket.channel;
    var result = channel.write(content, 0);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        io:IOError err => {
            return err;
        }
    }
}

function read (int size) returns (blob, int) | io:IOError {
    io:ByteChannel channel = socket.channel;
    var result = channel.read(size);
    match result{
        (blob , int)  content => {
            var (bytes, numberOfBytes) = content;
            return (bytes, numberOfBytes);
        }
        io:IOError err => {
            return err;
        }
    }
}
