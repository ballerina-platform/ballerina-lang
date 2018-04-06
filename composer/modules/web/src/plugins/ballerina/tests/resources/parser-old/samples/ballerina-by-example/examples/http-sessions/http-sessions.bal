import ballerina/http;
import ballerina/lang.messages;

service<http> session {

    string key = "status";
    @http:resourceConfig {
        methods:["GET"]
    }
    resource sayHello (message m) {
        //createSessionIfAbsent() function returns an existing session for a valid session id, otherwise it returns a new session.
        http:Session session = http:createSessionIfAbsent(m);
        string result;
        message response = {};
        //Session status(new or already existing) is informed by isNew() as boolean value.
        if (http:isNew(session)) {
            result = "Say hello to a new session";
        } else {
            result = "Say hello to an existing session";
        }
        //Binds a string attribute to this session with a key(string).
        http:setAttribute(session, key, "Session sample");
        messages:setStringPayload(response, result);
        response:send(response);
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource doTask (message m) {
        //getSession() returns an existing session for a valid session id. otherwise null.
        http:Session session = http:getSession(m);
        string attributeValue;
        message response = {};
        if (session != null) {
            //Returns the object bound with the specified key.
            attributeValue, _ = (string)http:getAttribute(session, key);
        } else {
            attributeValue = "Session unavailable";
        }
        messages:setStringPayload(response, attributeValue);
        response:send(response);
    }

    @http:resourceConfig {
        methods:["GET"]
    }
    resource sayBye (message m) {
        http:Session session = http:getSession(m);
        message response = {};
        if (session != null) {
            //Returns session id.
            string id = http:getId(session);
            //Invalidates this session.
            http:invalidate(session);
            messages:setStringPayload(response
                            , "Session: " + id + " invalidated");
        } else {
            messages:setStringPayload(response, "Session unavailable");
        }
        response:send(response);
    }
}

