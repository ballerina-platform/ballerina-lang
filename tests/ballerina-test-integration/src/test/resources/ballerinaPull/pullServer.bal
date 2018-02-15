import ballerina.file;
import ballerina.io;
import ballerina.net.http;
import ballerina.log;

@http:configuration {
    basePath:"/echo"
}
service<http> echo {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        log:printInfo("Resourceeeeeeeeee 111111");
        http:OutResponse res = {};
        json reqPayload = req.getJsonPayload();
        string srcFilePath = reqPayload.payload.toString();
        io:ByteChannel src = getFileChannel(srcFilePath, "r");
        int bytesChunk = 10000;
        blob contentAsBytes = readBytes(src, bytesChunk);
        res.setBinaryPayload(contentAsBytes);
        _ = conn.respond(res);
        log:printInfo("Resourceeeeeeeeee 2222");
    }
}

function getFileChannel (string filePath, string permission) (io:ByteChannel) {
    file:File src = {path:filePath};
    io:ByteChannel channel = src.openChannel(permission);
    return channel;
}

function readBytes (io:ByteChannel channel, int numberOfBytes) (blob) {
    blob bytes;
    int numberOfBytesRead;
    bytes, numberOfBytesRead = channel.readBytes(numberOfBytes);
    return bytes;
}