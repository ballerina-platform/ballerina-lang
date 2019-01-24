import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

// Creates an endpoint for the client.
http:Client clientEndpoint = new("http://localhost:9090");

@http:ServiceConfig {
    basePath: "/stream"
}
service HTTPStreamingService on new http:Listener(9090) {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/fileupload"
    }
    resource function handleOutputStream(http:Caller caller,
                                         http:Request clientRequest) {
        http:Request request = new;

        //Sets the file as request payload.
        request.setFileAsPayload("./files/BallerinaLang.pdf",
            contentType = mime:APPLICATION_PDF);

        //Sends the request to the client with the file content.
        var clientResponse = clientEndpoint->post("/stream/receiver", request);

        http:Response res = new;
        if(clientResponse is http:Response) {
            var payload = clientResponse.getTextPayload();
            if (payload is string) {
                res.setPayload(untaint payload);
            } else {
                setError(res, payload);
            }
        } else {
            log:printError("Error occurred while sending data to the client ",
                            err = clientResponse);
            setError(res, clientResponse);
        }
        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error while while sending response to the caller",
                            err = result);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/receiver"
    }
    resource function handleInputStream(http:Caller caller,
                                        http:Request clientRequest) {
        http:Response res = new;
        var payload = clientRequest.getByteChannel();
        if (payload is io:ReadableByteChannel) {
            //Writes the incoming stream to a file. First get the destination
            //channel by providing the file name, the content should be
            //written to.
            io:WritableByteChannel destinationChannel =
                io:openWritableFile("./files/ReceivedFile.pdf");
            var result = copy(payload, destinationChannel);
            if (result is error) {
                log:printError("error occurred while performing copy ",
                                err = result);
            }
            close(payload);
            close(destinationChannel);
            res.setPayload("File Received!");
        } else {
            setError(res, payload);
        }
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error occurred while sending response",
                           err = result);
        }
    }
}

//Sets the error to the response.
function setError(http:Response res, error err) {
    res.statusCode = 500;
    res.setPayload(untaint string.convert(err.detail().message));
}

// Copies the content from the source channel to the destination channel.
function copy(io:ReadableByteChannel src, io:WritableByteChannel dst)
             returns error? {
    int readCount = 1;
    byte[] readContent;
    while (readCount > 0) {
        //Operation attempts to read a maximum of 1000 bytes.
        (byte[], int) result = check src.read(1000);
        (readContent, readCount) = result;
        //Writes the given content into the channel.
        var writeResult = check dst.write(readContent, 0);
    }
    return;
}

//Closes the byte channel.
function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    abstract object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}
