import ballerina/http;
import ballerina/io;
import ballerina/system;

# Defines the endpoint to update the access token.
endpoint http:Listener listener {
    port:9295
};

# This function opens the file for writing content.
#
# + filePath - File path
# + encoding - Encoding
# + return - CharacterChannel of the file after writing to the file
function openForWriting (string filePath, string encoding) returns io:CharacterChannel {
    io:ByteChannel channel = io:openFile(filePath, "w");
    io:CharacterChannel result = new io:CharacterChannel(channel, encoding);
    return result;
}

# This service updates the access token.
@http:ServiceConfig {
    endpoints:[listener],
    basePath:"/update-settings"
}
service<http:Service> update_token bind listener {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/{token}"
    }
    # Updates the access token.
    #
    # + caller - Endpoint
    # + request - Request object
    # + token - Access token
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
