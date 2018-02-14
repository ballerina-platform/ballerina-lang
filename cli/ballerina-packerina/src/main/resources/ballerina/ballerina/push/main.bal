package ballerina.push;

import ballerina.compression;
import ballerina.mime;
import ballerina.net.http;

function main (string[] args) {

    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient(args[0], {});
    }
    mime:Entity topLevelEntity = {};
    mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_FORM_DATA);
    topLevelEntity.contentType = mediaType;

    blob compressedContent = compression:zipToBytes(args[1]);
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    filePart.byteData = compressedContent;
    mime:Entity[] bodyParts = [filePart];

    topLevelEntity.multipartData = bodyParts;
    http:OutRequest request = {};
    request.setEntity(topLevelEntity);
    http:InResponse resp1 = {};
    resp1, _ = httpEndpoint.post("", request);
    println("Ballerina package pushed successfully");
}