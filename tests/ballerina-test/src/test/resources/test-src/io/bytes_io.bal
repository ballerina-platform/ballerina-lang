import ballerina/io;

io:ByteChannel|null channel;

function initFileChannel (string filePath, string permission) {
    channel = io:openFile(filePath, permission);
}

function readBytes (int numberOfBytes) returns (blob|io:IOError) {
    blob empty;
    match channel{
        io:ByteChannel byteChannel =>{
            var result = byteChannel.read(numberOfBytes);
            match result {
                (blob,int) content =>{
                    var (bytes, numberOfBytes) = content;
                    return bytes;
                }
                io:IOError err =>{
                    return err;
                }
            }
        }
        (any|null)=>{
            return empty;
        }
    }
}

function writeBytes (blob content, int startOffset) returns (int|io:IOError) {
    int empty = -1;
    match channel {
        io:ByteChannel byteChannel =>{
            var result = byteChannel.write(content, startOffset);
            match result {
                int numberOfBytesWritten =>{
                    return numberOfBytesWritten;
                }
                io:IOError err =>{
                    return err;
                }
            }
        }
        (any|null) =>{
            return empty;
        }
    }
}

function close () {
    match channel {
        io:ByteChannel byteChannel =>{
            io:IOError err = byteChannel.close();
        }
        (any|null) =>{
            io:println("Bytes stream cannot be closed");
        }
    }

}
