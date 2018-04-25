import ballerina/http;
import ballerina/io;
import ballerina/system;

endpoint http:Listener listener {
    port:9295
};

function openForWriting (string filePath, string encoding) returns io:CharacterChannel {
    io:ByteChannel channel = io:openFile(filePath, "w");
    io:CharacterChannel result = new io:CharacterChannel(channel, encoding);
    return result;
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
    one_px_image (endpoint caller, http:Request request, string token) {
        http:Response response = new;
        response.setHeader("Content-Type", "image/svg+xml");
        response.setPayload("<svg xmlns=\"http://www.w3.org/2000/svg\"/>");
        var destinationChannel = openForWriting(system:getUserHome() + "/.ballerina/Settings.toml", "UTF-8");
        var result = destinationChannel.write("[central]\naccesstoken= \"" + token  + "\"", 0);
        io:println("Token updated");
        _ = caller -> respond(response);
    }
}
