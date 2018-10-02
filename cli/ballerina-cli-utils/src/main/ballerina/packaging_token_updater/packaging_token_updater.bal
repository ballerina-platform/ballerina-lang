import ballerina/http;
import ballerina/io;
import ballerina/system;

# This function opens the file for writing content.
#
# + filePath - File path
# + encoding - Encoding
# + return - CharacterChannel of the file after writing to the file
function openForWriting (string filePath, string encoding) returns io:WritableCharacterChannel {
    io:WritableByteChannel byteChannel = io:openWritableFile(filePath);
    io:WritableCharacterChannel result = new io:WritableCharacterChannel(byteChannel, encoding);
    return result;
}

# This service updates the access token.
@http:ServiceConfig {
    basePath:"/update-settings"
}
service<http:Service> update_token bind { port: 9295 } {

    # Updates the access token.
    #
    # + caller - Endpoint
    # + request - Request object
    # + token - Access token
    @http:ResourceConfig {
        methods:["GET"],
        path:"/{token}"
    }
    one_px_image (endpoint caller, http:Request request, string token) {
        http:Response response = new;
        response.setPayload("<svg xmlns=\"http://www.w3.org/2000/svg\"/>");
        response.setHeader("Content-Type", "image/svg+xml");
        var destinationChannel = openForWriting(system:getUserHome() + "/.ballerina/Settings.toml", "UTF-8");
        var result = destinationChannel.write("[central]\naccesstoken= \"" + token  + "\"", 0);
        io:println("Token updated");
        _ = caller -> respond(response);
    }
}
