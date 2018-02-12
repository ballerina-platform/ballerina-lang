import ballerina.compression;
import ballerina.mime;
import ballerina.net.http;

function main (string[] args) {

    endpoint<http:HttpClient> httpEndpoint {
        create http:HttpClient("http://localhost:9090/foo/receivableParts", {});
    }
    mime:Entity topLevelEntity = {};
    mime:MediaType mediaType = mime:getMediaType(mime:MULTIPART_FORM_DATA);
    topLevelEntity.contentType = mediaType;

    // Destination directory
    mime:Entity destDirText = {};
    destDirText.textData = args[1];
    mime:MediaType contentTypeOfText = mime:getMediaType(mime:TEXT_PLAIN);
    destDirText.contentType = contentTypeOfText;

    // Content
    blob compressedContent = compression:zipToBytes(args[0]);
    mime:Entity filePart = {};
    mime:MediaType contentTypeOfFilePart = mime:getMediaType(mime:APPLICATION_OCTET_STREAM);
    filePart.contentType = contentTypeOfFilePart;
    filePart.byteData = compressedContent;

    mime:Entity[] bodyParts = [destDirText, filePart];

    topLevelEntity.multipartData = bodyParts;
    http:OutRequest request = {};
    request.setEntity(topLevelEntity);
    http:InResponse resp1 = {};
    resp1, _ = httpEndpoint.post("", request);
}