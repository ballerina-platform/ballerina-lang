import ballerina/io;

io:Socket socket;

function openSocketConnection (string host, int port) {
    io:SocketProperties properties = {localPort:0};
    var result = io:openSocket(host, port, properties);
    match result {
        io:Socket s => {
            socket = s;
        }
        error err => {
            throw err;
        }
    }
}

function openSocketConnectionWithProps (string host, int port, io:SocketProperties prop) returns (io:Socket|error) {
    var result = io:openSocket(host, port, prop);
    match result {
        io:Socket s => {
            return s;
        }
        error err => {
            return err;
        }
    }
}

function closeSocket () {
    error? err = socket.close();
}

function write (blob content) returns (int | error) {
    io:ByteChannel channel = socket.channel;
    var result = channel.write(content, 0);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        error err => {
            return err;
        }
    }
}

function read (int size) returns (blob, int) | error {
    io:ByteChannel channel = socket.channel;
    var result = channel.read(size);
    match result{
        (blob , int)  content => {
            var (bytes, numberOfBytes) = content;
            return (bytes, numberOfBytes);
        }
        error err => {
            return err;
        }
    }
}

function close (io:Socket socket) {
    error? err = socket.close();
}
