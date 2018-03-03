import ballerina.mime;
import ballerina.net.http;
import ballerina.compression;
import ballerina.log;
@http:configuration {basePath:"/foo"}
service<http> echo {
    @http:resourceConfig {
        methods:["POST"],
        path:"/receivableParts"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        string destFilePath;
        mime:Entity[] bodyParts = req.getMultiparts();
        int i = 0;
        while (i < lengthof bodyParts) {
            mime:Entity part = bodyParts[i];
            string contentType = part.contentType.toString();
            if (mime:TEXT_PLAIN == contentType) {
                destFilePath = mime:getText(part);
            } else if (mime:APPLICATION_OCTET_STREAM == contentType) {
                compression:unzipBytes(mime:getBlob(part), destFilePath, "");
                log:printInfo("Content saved to file");
            }
            i = i + 1;
        }
        http:OutResponse res = {};
        res.setStringPayload("Multiparts Received!");
        _ = conn.respond(res);
    }
}
