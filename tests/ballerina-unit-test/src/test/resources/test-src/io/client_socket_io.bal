import ballerina/io;

io:Socket socket;

function openSocketConnection (string host, int port) {
    io:Socket s = new;
    check s.connect(host, port);
    socket = s;
}

function openSocketConnectionWithProps (string host, int port, int localPort) returns io:Socket {
    io:Socket s = new;
    check s.bindAddress(localPort);
    check s.connect(host, port);
    return s;
}

function closeSocket () {
    error? err = socket.close();
}

function write (byte[] content) returns int|error {
    io:ByteChannel byteChannel = socket.byteChannel;
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

function read (int size) returns (byte[], int)|error {
    io:ByteChannel byteChannel = socket.byteChannel;
    var result = byteChannel.read(size);
    match result{
        (byte[] , int)  content => {
            var (bytes, numberOfBytes) = content;
            io:println("Number of byte read from server: ", numberOfBytes);
            return (bytes, numberOfBytes);
        }
        error err => {
            return err;
        }
    }
}

function close (io:Socket localSocket) {
    error? err = localSocket.close();
}
