import ballerina.net.http;
import ballerina.lang.strings;
import ballerina.net.http.request;
import ballerina.net.http.response;

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
        response:setStringPayload(res, result);
        response:send(res);
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
        response:setStringPayload(res, result);
        response:send(res);
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
        response:setStringPayload(res, result);
        response:send(res);
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
        response:setStringPayload(res, result);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test5"
    }
    resource echo5 (http:Request req, http:Response res) {

        string result = "chamil";
        http:Session session = http:getSession(req);
        any attribute = http:getAttribute(session, "name");
        response:setStringPayload(res, result);
        response:send(res);
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
        response:setStringPayload(res, myName);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/hello"
    }
    resource hello (http:Request req, http:Response res) {

        TypeCastError err;
        string result = request:getStringPayload(req);
        http:Session session = http:createSessionIfAbsent(req);
        any attribute = http:getAttribute(session, "name");
        if (attribute != null) {
            result, err = (string)attribute;

        } else {
            http:setAttribute(session, "name", result);
        }
        response:setStringPayload(res, result);
        response:send(res);
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
        response:setStringPayload(res, strings:valueOf(sessionCounter));
        response:send(res);
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
        response:setStringPayload(res, strings:valueOf(sessionCounter));
        response:send(res);
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
        response:setStringPayload(res, myName);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource myStruct (http:Request req, http:Response res) {

        string result = request:getStringPayload(req);
        TypeCastError err;
        Data d = {name:result};
        http:Session Session = http:createSessionIfAbsent(req);
        any attribute = http:getAttribute(Session, "nameStruct");
        if (attribute != null) {
            //d, err = (Data)attribute;
        } else {
            http:setAttribute(Session, "nameStruct", d);
        }
        response:setStringPayload(res, d.name);
        response:send(res);
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
        response:setStringPayload(res, "arraysize:" + arrsize);
        response:send(res);
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
        response:setStringPayload(res, arr[0]);
        response:send(res);
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
        response:setStringPayload(res, strings:valueOf(arrsize));
        response:send(res);
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
        response:setStringPayload(res, strings:valueOf(arrsize));
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names5"
    }
    resource keyNames5 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        string[] arr = http:getAttributeNames(ses);
        int arrsize = lengthof arr;
        response:setStringPayload(res, strings:valueOf(arrsize));
        response:send(res);
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
        response:setStringPayload(res, strings:valueOf(arrsize));
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/id1"
    }
    resource id1 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        string id = http:getId(ses);
        response:setStringPayload(res, id);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/id2"
    }
    resource id2 (http:Request req, http:Response res) {

        http:Session ses = http:getSession(req);
        string id = http:getId(ses);
        response:setStringPayload(res, id);
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new1"
    }
    resource new1 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        boolean stat = http:isNew(ses);
        response:setStringPayload(res, strings:valueOf(stat));
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new2"
    }
    resource new2 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        int time = http:getCreationTime(ses);
        response:setStringPayload(res, strings:valueOf(time));
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new3"
    }
    resource new3 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:invalidate(ses);
        int time = http:getCreationTime(ses);
        response:setStringPayload(res, strings:valueOf(time));
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new4"
    }
    resource new4 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        int time = http:getLastAccessedTime(ses);
        response:setStringPayload(res, strings:valueOf(time));
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new5"
    }
    resource new5 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:invalidate(ses);
        int time = http:getLastAccessedTime(ses);
        response:setStringPayload(res, strings:valueOf(time));
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new6"
    }
    resource new6 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        int time = http:getMaxInactiveInterval(ses);
        http:setMaxInactiveInterval(ses, 60);
        response:setStringPayload(res, strings:valueOf(time));
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new7"
    }
    resource new7 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        http:invalidate(ses);
        http:setMaxInactiveInterval(ses, 89);
        response:setStringPayload(res, "done");
        response:send(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new8"
    }
    resource new8 (http:Request req, http:Response res) {

        http:Session ses = http:createSessionIfAbsent(req);
        int time = http:getMaxInactiveInterval(ses);
        http:setMaxInactiveInterval(ses, -1);
        response:setStringPayload(res, strings:valueOf(time));
        response:send(res);
    }
}
