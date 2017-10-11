import ballerina.net.http;
import ballerina.lang.strings;

struct Data {
    string name;
}

@http:configuration {basePath:"/sample"}
service<http> sample {
    @http:resourceConfig {
        methods:["GET"],
        path:"/test1"
    }
    resource echo (http:Request req, http:Response res) {

        string result = "";
        http:Session session = http:createSessionIfAbsent(req);
        if (session != null) {
            result = "session created";
        }
        res.setStringPayload(result);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test2"
    }
    resource echo2 (http:Request req, http:Response res) {

        string result = "";
        http:Session session = http:getSession(req);
        if (session != null) {
            result = "session is returned";
        } else {
            result = "no session id available";
        }
        res.setStringPayload(result);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test3"
    }
    resource echo3 (http:Request req, http:Response res) {

        string result = "";
        http:Session session = http:getSession(req);
        if (session != null) {
            result = "session created";
        } else {
            result = "session is not created";
        }
        res.setStringPayload(result);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test4"
    }
    resource testGetAt (http:Request req, http:Response res) {

        string result = "";
        http:Session session = http:createSessionIfAbsent(req);
        any attribute = http:getAttribute(session, "name");
        if (attribute != null) {
            result = "attribute available";
        } else {
            result = "attribute not available";
        }
        res.setStringPayload(result);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test5"
    }
    resource echo5 (http:Request req, http:Response res) {

        string result = "chamil";
        http:Session session = http:getSession(req);
        any attribute = http:getAttribute(session, "name");
        res.setStringPayload(result);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test6"
    }
    resource echo6 (http:Request req, http:Response res) {

        string myName = "chamil";
        TypeCastError err;
        http:Session session1 = http:createSessionIfAbsent(req);
        http:Session session2 = http:createSessionIfAbsent(req);
        http:setAttribute(session1, "name", "wso2");
        any attribute = http:getAttribute(session2, "name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        res.setStringPayload(myName);
        res.send();
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/hello"
    }
    resource hello (http:Request req, http:Response res) {

        TypeCastError err;
        string result = req.getStringPayload();
        http:Session session = http:createSessionIfAbsent(req);
        any attribute = http:getAttribute(session, "name");
        if (attribute != null) {
            result, err = (string)attribute;

        } else {
            http:setAttribute(session, "name", result);
        }
        res.setStringPayload(result);
        res.send();
    }
}

@http:configuration {basePath:"/counter"}
service<http> counter {
    @http:resourceConfig {
        methods:["GET"],
        path:"/echo"
    }
    resource echoCount (http:Request req, http:Response res) {

        int sessionCounter;
        TypeCastError err;
        http:Session ses = http:createSessionIfAbsent(req);
        if (http:getAttribute(ses, "Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int)http:getAttribute(ses, "Counter");
        }
        sessionCounter = sessionCounter + 1;
        http:setAttribute(ses, "Counter", sessionCounter);
        res.setStringPayload(strings:valueOf(sessionCounter));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    resource echoCount2 (http:Request req, http:Response res) {

        int sessionCounter;
        TypeCastError err;
        http:Session ses = http:getSession(req);
        if (http:getAttribute(ses, "Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int)http:getAttribute(ses, "Counter");
        }
        sessionCounter = sessionCounter + 1;
        http:setAttribute(ses, "Counter", sessionCounter);
        res.setStringPayload(strings:valueOf(sessionCounter));
        res.send();
    }
}

@http:configuration {basePath:"/sample2"}
service<http> sample2 {
    @http:resourceConfig {
        methods:["GET"]
    }
    resource echoName (http:Request req, http:Response res) {
        string myName = "wso2";
        TypeCastError err;
        http:Session Session = http:createSessionIfAbsent(req);
        any attribute = http:getAttribute(Session, "name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        http:setAttribute(Session, "name", "chamil");
        res.setStringPayload(myName);
        res.send();
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource myStruct (http:Request req, http:Response res) {

        string result = req.getStringPayload();
        TypeCastError err;
        Data d = {name:result};
        http:Session Session = http:createSessionIfAbsent(req);
        any attribute = http:getAttribute(Session, "nameStruct");
        if (attribute != null) {
            //d, err = (Data)attribute;
        } else {
            http:setAttribute(Session, "nameStruct", d);
        }
        res.setStringPayload(d.name);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names"
    }
    resource keyNames (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:setAttribute(ses, "Counter", "1");
        http:setAttribute(ses, "Name", "chamil");
        string[] arr = http:getAttributeNames(ses);
        int arrsize = lengthof arr;
        res.setStringPayload("arraysize:" + arrsize);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names2"
    }
    resource keyNames2 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:setAttribute(ses, "Counter", "1");
        http:setAttribute(ses, "location", "colombo");
        string[] arr = http:getAttributeNames(ses);
        res.setStringPayload(arr[0]);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names3"
    }
    resource keyNames3 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:setAttribute(ses, "location", "colombo");
        http:setAttribute(ses, "channel", "yue");
        http:setAttribute(ses, "month", "june");
        http:setAttribute(ses, "Name", "chamil");
        http:removeAttribute(ses, "Name");
        string[] arr = http:getAttributeNames(ses);
        int arrsize = lengthof arr;
        res.setStringPayload(strings:valueOf(arrsize));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names4"
    }
    resource keyNames4 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:setAttribute(ses, "Counter", "1");
        http:setAttribute(ses, "Name", "chamil");
        http:removeAttribute(ses, "Name");
        http:invalidate(ses);
        string[] arr = http:getAttributeNames(ses);
        int arrsize = lengthof arr;
        res.setStringPayload(strings:valueOf(arrsize));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names5"
    }
    resource keyNames5 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        string[] arr = http:getAttributeNames(ses);
        int arrsize = lengthof arr;
        res.setStringPayload(strings:valueOf(arrsize));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names6"
    }
    resource keyNames6 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:setAttribute(ses, "location", "colombo");
        http:removeAttribute(ses, "Name");
        string[] arr = http:getAttributeNames(ses);
        int arrsize = lengthof arr;
        res.setStringPayload(strings:valueOf(arrsize));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/id1"
    }
    resource id1 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        string id = http:getId(ses);
        res.setStringPayload(id);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/id2"
    }
    resource id2 (http:Request req, http:Response res) {

        http:Session ses = http:getSession(req);
        string id = http:getId(ses);
        res.setStringPayload(id);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new1"
    }
    resource new1 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        boolean stat = http:isNew(ses);
        res.setStringPayload(strings:valueOf(stat));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new2"
    }
    resource new2 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        int time = http:getCreationTime(ses);
        res.setStringPayload(strings:valueOf(time));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new3"
    }
    resource new3 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:invalidate(ses);
        int time = http:getCreationTime(ses);
        res.setStringPayload(strings:valueOf(time));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new4"
    }
    resource new4 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        int time = http:getLastAccessedTime(ses);
        res.setStringPayload(strings:valueOf(time));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new5"
    }
    resource new5 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:invalidate(ses);
        int time = http:getLastAccessedTime(ses);
        res.setStringPayload(strings:valueOf(time));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new6"
    }
    resource new6 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        int time = http:getMaxInactiveInterval(ses);
        http:setMaxInactiveInterval(ses, 60);
        res.setStringPayload(strings:valueOf(time));
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new7"
    }
    resource new7 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:invalidate(ses);
        http:setMaxInactiveInterval(ses, 89);
        res.setStringPayload("done");
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new8"
    }
    resource new8 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        int time = http:getMaxInactiveInterval(ses);
        http:setMaxInactiveInterval(ses, -1);
        res.setStringPayload(strings:valueOf(time));
        res.send();
    }
}
