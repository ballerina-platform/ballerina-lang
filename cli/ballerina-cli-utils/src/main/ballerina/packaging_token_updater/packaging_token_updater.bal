import ballerina/http;
import ballerina/io;
import ballerina/system;

documentation {
    Defines the endpoint to update the access token.
}
endpoint http:Listener listener {
    port:9295
};

documentation {
    This function opens the file for writing content.

    P{{filePath}} File path
    P{{encoding}} Encoding
    R{{}} CharacterChannel of the file after writing to the file
}
function openForWriting (string filePath, string encoding) returns io:CharacterChannel {
    io:ByteChannel byteChannel = io:openFile(filePath, "w");
    io:CharacterChannel result = new io:CharacterChannel(byteChannel, encoding);
    return result;
}

documentation{
    This service updates the access token.
}
@http:ServiceConfig {
    endpoints:[listener],
    basePath:"/update-settings"
}
service<http:Service> update_token bind listener {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/{token}"
    }
    documentation{
        Updates the access token.

        P{{caller}} Endpoint
        P{{request}} Request object
        P{{token}} Access token
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
