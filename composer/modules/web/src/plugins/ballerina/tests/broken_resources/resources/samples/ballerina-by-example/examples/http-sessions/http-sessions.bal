import ballerina/http;
import ballerina/lang.messages;

service<http> session {
    @http:GET {}
    resource sayHello (message m) {
        //createSessionIfAbsent() function returns an existing session for a valid session id, otherwise it returns a new session.
        http:Session session = http:createSessionIfAbsent(m);
        string result;
        //Session status(new or already existing) is informed by isNew() as boolean value.
        if (session != null && http:isNew(session)) {
            result = "Say hello to a new session";
            //Gets the session creation time in milliseconds.
            int createTime = http:getCreationTime(session);
            //Sets inactive time interval in seconds.
            http:setMaxInactiveInterval(session, 1000);
        }
        //Binds an object in any type to this session with a key(string).
        http:setAttribute(session, "key", "Session example");
        messages:setStringPayload(m, result);
        reply m;
    }

    @http:GET {}
    resource welcome (message m) {
        //getSession() returns an existing session for a valid session id. otherwise null.
        http:Session session = http:getSession(m);
        string attributeValue;

        if (session != null) {
            //Returns an array of Strings containing the keys of all the objects.
            string[] arr = http:getAttributeNames(session);
            //Returns the object bound with the specified key.
            attributeValue, _ = (string)http:getAttribute(session, arr[0]);
            //Gets the session last accessed time in milliseconds.
            int lastAccessedTime = http:getLastAccessedTime(session);
        }
        messages:setStringPayload(m, "Hi " + attributeValue);
        reply m;
    }

    @http:GET {}
    resource sayBye (message m) {
        http:Session session = http:getSession(m);
        string id;
        if (session != null) {
            //Gets the session max inactive interval in seconds.
            int inactiveInterval = http:getMaxInactiveInterval(session);
            //Returns session id.
            id = http:getId(session);
            //Removes the object bound with the specified key from this session.
            http:removeAttribute(session, "key");
        }
        
        //Invalidates this session.
        http:invalidate(session);
        messages:setStringPayload(m, "Session: " + id + " invalidated");
        reply m;
    }
}

