import ballerina/http;
import ballerina/io;

endpoint http:Listener sessionTestEP {
    port:9090
};

@http:ServiceConfig { basePath:"/sessionTest" }
service<http:Service> sessionTest bind sessionTestEP {

    string key = "status";
    @http:ResourceConfig {
        methods:["GET"],
        path:"/sayHello"
    }
    sayHello (endpoint outboundEP, http:Request req) {
        //The 'createSessionIfAbsent()' function returns an existing session for a valid session ID. If the session ID does not exist, it returns a new session.
        http:Session session = req.createSessionIfAbsent();
        string result;
        //This returns the session status (i.e., new or already existing) as a boolean value via the 'isNew()' function.
        if (session.isNew()) {
            result = "Say hello to a new session";
        } else {
            result = "Say hello to an existing session";
        }
        //This binds a string attribute to this session with a key(string).
        session.setAttribute(key, "Session sample");
        http:Response res = new;
        res.setStringPayload(result);
        _ = outboundEP -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/doTask"
    }
    doTask (endpoint outboundEP, http:Request req) {
        //getSession() returns an existing session for a valid session id. otherwise null.
        http:Session session = req.getSession();
        string attributeValue;
        if (session != null) {
            //This returns the object bound with the specified key.
            attributeValue = <string>session.getAttribute(key);
        } else {
            attributeValue = "Session unavailable";
        }
        http:Response res = new;
        res.setStringPayload(attributeValue);
        _ = outboundEP -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/sayBye"
    }
    sayBye (endpoint outboundEP, http:Request req) {
        http:Session session = req.getSession();
        http:Response res = new;
        if (session != null) {
            //This returns the session ID.
            string id = session.getId();
            //This invalidates this session.
            session.invalidate();
            res.setStringPayload("Session: " + id + " invalidated");
        } else {
            res.setStringPayload("Session unavailable");
        }
        _ = outboundEP -> respond(res);
    }
}
