import ballerina.net.http;
import ballerina.net.http.response;

service<http> session {

    string key = "status";
    @http:resourceConfig {
        methods:["GET"]
    }
    resource sayHello (http:Request req, http:Response res) {
        //createSessionIfAbsent() function returns an existing session for a valid session id, otherwise it returns a new session.
        http:Session session = http:createSessionIfAbsent(req);
        string result;
        //Session status(new or already existing) is informed by isNew() as boolean value.
        if (http:isNew(session)) {
            result = "Say hello to a new session";
        } else {
            result = "Say hello to an existing session";
        }
        //Binds a string attribute to this session with a key(string).
        http:setAttribute(session, key, "Session sample");
        response:setStringPayload(res, result);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource doTask (http:Request req, http:Response res) {
        //getSession() returns an existing session for a valid session id. otherwise null.
        http:Session session = http:getSession(req);
        string attributeValue;
        if (session != null) {
            //Returns the object bound with the specified key.
            attributeValue, _ = (string)http:getAttribute(session, key);
        } else {
            attributeValue = "Session unavailable";
        }
        response:setStringPayload(res, attributeValue);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource sayBye (http:Request req, http:Response res) {
        http:Session session = http:getSession(req);
        if (session != null) {
            //Returns session id.
            string id = http:getId(session);
            //Invalidates this session.
            http:invalidate(session);
            response:setStringPayload(res
                            , "Session: " + id + " invalidated");
        } else {
            response:setStringPayload(res, "Session unavailable");
        }
        response:send(res);
    }
}

