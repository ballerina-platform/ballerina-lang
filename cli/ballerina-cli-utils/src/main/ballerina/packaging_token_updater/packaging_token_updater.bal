import ballerina/http;
import ballerina/io;
import ballerina/system;

documentation {
    This function opens the file for writing content.

    P{{filePath}} File path
    P{{encoding}} Encoding
    R{{}} CharacterChannel of the file after writing to the file
}
function openForWriting (string filePath, string encoding) returns io:WritableCharacterChannel {
    io:WritableByteChannel byteChannel = io:openFileForWriting(filePath);
    io:WritableCharacterChannel result = new io:WritableCharacterChannel(byteChannel, encoding);
    return result;
}

documentation{
    This service updates the access token.
}
@http:ServiceConfig {
    basePath:"/update-settings"
}
service<http:Service> update_token bind { port: 9295 } {

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
