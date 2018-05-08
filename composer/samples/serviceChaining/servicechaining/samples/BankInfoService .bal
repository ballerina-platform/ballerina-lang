
import ballerina/http;

@http:configuration {basePath:"/bankinfo"}
service<http> Bankinfo {

    @http:resourceConfig {
        methods:["POST"]
    }
    resource product (http:Connection conn, http:Request req) {
        var jsonRequest, payloadError = req.getJsonPayload();
        http:Response res = {};
        if (payloadError == null) {
            string branchCode;
            branchCode, _ = (string)jsonRequest.BranchInfo.BranchCode;
            json payload = {};
            if (branchCode == "123") {
                payload = {"ABC Bank":{"Address":"111 River Oaks Pkwy, San Jose, CA 95999"}};
            } else {
                payload = {"ABC Bank":{"error":"No branches found."}};
            }
            res.setJsonPayload(payload);
        } else {
            res.statusCode = 500;
            res.setTextPayload(payloadError.message);
        }

        _ = conn.respond(res);
    }
}
