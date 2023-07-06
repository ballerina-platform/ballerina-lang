import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;

// Creates an endpoint for the [client](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/clients/Client.html).
http:Client clientEndpoint = new ("http://localhost:9090");

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

        //[Sets the file](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/classes/Request.html#setFileAsPayload) as the request payload.
        request.setFileAsPayload("./files/BallerinaLang.pdf",
            contentType = mime:APPLICATION_PDF);

        //Sends the request to the client with the file content.
        var clientResponse = clientEndpoint->post("/stream/receiver", request);

        http:Response res = new;
        if (clientResponse is http:Response) {
            var payload = clientResponse.getTextPayload();
            if (payload is string) {
                res.setPayload(<@untainted>payload);
            } else {
                setError(res, payload);
            }
        } else {
            log:printError("Error occurred while sending data to the client ",
                            <error>clientResponse);
            setError(res, <error>clientResponse);
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
            //Writes the incoming stream to a file. First, [get the destination channel](https://ballerina.io/swan-lake/learn/api-docs/ballerina/io/functions.html#openWritableFile)
            //by providing the file name to which the content should be written to.
            var destinationChannel =
                io:openWritableFile("./files/ReceivedFile.pdf");

            if (destinationChannel is io:WritableByteChannel) {
                var result = copy(payload, destinationChannel);
                if (result is error) {
                    log:printError("error occurred while performing copy ",
                                    err = result);
                }
                close(payload);
                close(destinationChannel);
            }
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
    res.setPayload(<@untainted>err.message());
}

// Copies the content from the source channel to a destination channel.
function copy(io:ReadableByteChannel src,
              io:WritableByteChannel dst) returns error? {
    // The below example shows how to read all the content from
    // the source and copy it to the destination.
    while (true) {
        // The operation attempts to [read](https://ballerina.io/swan-lake/learn/api-docs/ballerina/io/classes/ReadableByteChannel.html#read) a maximum of 1000 bytes and returns
        // with the available content, which could be < 1000.
        byte[]|io:Error result = src.read(1000);
        if (result is io:EofError) {
            break;
        } else if (result is error) {
            return <@untainted>result;
        } else {
            // The operation [writes](https://ballerina.io/swan-lake/learn/api-docs/ballerina/io/classes/WritableByteChannel.html#write) the given content into the channel.
            int i = 0;
            while (i < result.length()) {
                var result2 = dst.write(result, i);
                if (result2 is error) {
                    return result2;
                } else {
                    i = i + result2;
                }
            }
        }
    }
    return;
}

//Closes the byte channel.
function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occurred while closing the channel: ", cr);
    }
}
