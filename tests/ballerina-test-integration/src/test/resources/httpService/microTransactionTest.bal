import ballerina.net.http;
import ballerina.transactions.coordinator;

service<http> initiator1 {

    @http:resourceConfig {
        path:"/"
    }
    resource member (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090/participant1", {});
        }
        http:OutRequest newReq = {};
        http:InResponse clientResponse1;
        transaction {
            clientResponse1, _ = endPoint.get("/", newReq);
        } failed {
            println("Failed");
        }
        _ = conn.forward(clientResponse1);
    }
}

service<http> participant1 {

    @http:resourceConfig {
        path:"/"
    }
    resource member (http:Connection conn, http:InRequest req) {
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090/participant2", {});
        }
        http:OutRequest newReq = {};
        newReq.setHeader("participant-id", req.getHeader("X-XID"));
        http:InResponse clientResponse2;
        transaction {
            var clientResponse1, _ = endPoint.forward("/task1", req);
            clientResponse2, _ = endPoint.get("/task2", newReq);
        } failed {
            println("Failed");
        }
        _ = conn.forward(clientResponse2);
    }
}

service<http> participant2 {
    resource task1 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Resource is invoked");
        _ = conn.respond(res);
    }

    resource task2 (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        string result = "incorrect id";
        if (req.getHeader("X-XID") == req.getHeader("participant-id")) {
            result = "equal id";
        }
        res.setStringPayload(result);
        _ = conn.respond(res);
    }
}
