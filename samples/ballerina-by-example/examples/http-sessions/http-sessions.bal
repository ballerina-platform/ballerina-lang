import ballerina.net.http;

service<http> session {

    string key = "status";
    @http:resourceConfig {
        methods:["GET"]
    }
    resource sayHello (http:Request req, http:Response res) {
        //createSessionIfAbsent() function returns an existing session for a valid session id, otherwise it returns a new session.
        http:Session session = req.createSessionIfAbsent();
        string result;
        //Session status(new or already existing) is informed by isNew() as boolean value.
        if (session.isNew()) {
            result = "Say hello to a new session";
        } else {
            result = "Say hello to an existing session";
        }
        //Binds a string attribute to this session with a key(string).
        session.setAttribute(key, "Session sample");
        res.setStringPayload(result);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource doTask (http:Request req, http:Response res) {
        //getSession() returns an existing session for a valid session id. otherwise null.
        http:Session session = req.getSession();
        string attributeValue;
        if (session != null) {
            //Returns the object bound with the specified key.
            attributeValue, _ = (string)session.getAttribute(key);
        } else {
            attributeValue = "Session unavailable";
        }
        res.setStringPayload(attributeValue);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource sayBye (http:Request req, http:Response res) {
        http:Session session = req.getSession();
        if (session != null) {
            //Returns session id.
            string id = session.getId();
            //Invalidates this session.
            session.invalidate();
            res.setStringPayload("Session: " + id + " invalidated");
        } else {
            res.setStringPayload("Session unavailable");
        }
        res.send();
    }
}
