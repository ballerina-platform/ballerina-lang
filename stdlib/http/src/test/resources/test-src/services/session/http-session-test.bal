import ballerina/http;
import ballerina/mime;
import ballerina/io;

type Data record {
    string name;
};

listener http:MockListener sessionEP = new(9090);

@http:ServiceConfig {basePath:"/sample"}
service sample on sessionEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test1"
    }
    resource function echo(http:Caller caller, http:Request req) {
        string result = "";
        http:Session session = req.createSessionIfAbsent();
        if (session != null) {
            result = "session created";
        }
        http:Response res = new;
        res.setTextPayload(result);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test2"
    }
    resource function echo2(http:Caller caller, http:Request req) {
        string result = "";
        http:Session session = req.getSession();
        if (session != null) {
            result = "session is returned";
        } else {
            result = "no session id available";
        }
        http:Response res = new;
        res.setTextPayload(result);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test3"
    }
    resource function echo3(http:Caller caller, http:Request req) {
        string result = "";
        http:Session session = req.getSession();
        if (session != null) {
            result = "session created";
        } else {
            result = "session is not created";
        }
        http:Response res = new;
        res.setTextPayload(result);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test4"
    }
    resource function testGetAt(http:Caller caller, http:Request req) {
        string result = "";
        http:Session session = req.createSessionIfAbsent();
        any attribute = session.getAttribute("name");
        if (attribute != null) {
            result = "attribute available";
        } else {
            result = "attribute not available";
        }
        http:Response res = new;
        res.setTextPayload(result);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test5"
    }
    resource function echo5(http:Caller caller, http:Request req) {
        http:Response res = new;
        string result = "testValue";
        http:Session session = req.getSession();
        if (session != null) {
            any attribute = session.getAttribute("name");
            res.setTextPayload(<string>attribute);
        } else {
            res.setTextPayload(result);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test6"
    }
    resource function echo6(http:Caller caller, http:Request req) {
        string myName = "chamil";
        http:Session session1 = req.createSessionIfAbsent();
        http:Session session2 = req.createSessionIfAbsent();
        session1.setAttribute("name", "wso2");
        any attribute = session2.getAttribute("name");
        if (attribute != null) {
            myName = <string>attribute;
        }
        http:Response res = new;
        res.setTextPayload(myName);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/hello"
    }
    resource function hello(http:Caller caller, http:Request req) {
        http:Response res = new;
        string result;
        var payload = req.getTextPayload();
        if (payload is string) {
            result = textPayload;
            http:Session session = req.createSessionIfAbsent();
            any attribute = session.getAttribute("name");
            if (attribute != null) {
                result = string.convert(attribute);
            } else {
                session.setAttribute("name", result);
            }
            res.setTextPayload(result);
        } else if (payload is error) {
            res.setTextPayload("Error");
        }
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {basePath:"/counter"}
service counter on sessionEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo"
    }
    resource function echoCount(http:Caller caller, http:Request req) {
        int sessionCounter;
        http:Session session = req.createSessionIfAbsent();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            var result = int.convert(session.getAttribute("Counter"));
            if (result is int) {
                sessionCounter = value;
            } else if (result is error) {
                io:println("Error occurred during int to string conversion");
            }
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);

        http:Response res = new;
        res.setTextPayload(string.convert(sessionCounter));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    resource function echoCount2(http:Caller caller, http:Request req) {
        int sessionCounter;
        http:Session session = req.getSession();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            var result = int.convert(session.getAttribute("Counter"));
            if (result is int) {
                sessionCounter = value;
            } else if (result is error) {
                io:println("Error occurred during int to string conversion");
            }
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);

        http:Response res = new;
        res.setTextPayload(<string>(sessionCounter));
        checkpanic caller->respond(res);
    }
}

@http:ServiceConfig {basePath:"/sample2"}
service sample2 on sessionEP {
    @http:ResourceConfig {
        methods:["GET"]
    }
    resource function echoName(http:Caller caller, http:Request req) {
        string myName = "wso2";
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName = <string>attribute;
        }
        Session.setAttribute("name", "chamil");

        http:Response res = new;
        res.setTextPayload(myName);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"]
    }
    resource function myStruct (http:Caller caller, http:Request req) {
        http:Response res = new;
        var payload = req.getTextPayload();
        if (payload is string) {
            Data d = {name:payload};
            http:Session Session = req.createSessionIfAbsent();
            any attribute = Session.getAttribute("nameStruct");
            if (attribute != null) {
                var result = Data.convert(attribute);
                if (result is Data) {
                    d = returnData;
                } else if (result is error) {
                    io:println("Error occurred!");
                }
            } else {
                Session.setAttribute("nameStruct", d);
            }
            res.setTextPayload(d.name);
        } else if (payload is error) {
            res.setTextPayload("Error");
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names"
    }
    resource function keyNames(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        string[] arr = session.getAttributeNames();
        int arrsize = arr.length();

        http:Response res = new;
        res.setTextPayload("arraysize:" + arrsize);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names2"
    }
    resource function keyNames2(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("location", "colombo");
        string[] arr = session.getAttributeNames();

        http:Response res = new;
        res.setTextPayload(arr[0]);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names3"
    }
    resource function keyNames3(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.setAttribute("channel", "yue");
        session.setAttribute("month", "june");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = arr.length();

        http:Response res = new;
        res.setTextPayload(<string>(arrsize));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names4"
    }
    resource function keyNames4(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        session.invalidate();
        string[] arr = session.getAttributeNames();
        int arrsize = arr.length();

        http:Response res = new;
        res.setTextPayload(<string>(arrsize));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names5"
    }
    resource function keyNames5(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        string[] arr = session.getAttributeNames();
        int arrsize = arr.length();

        http:Response res = new;
        res.setTextPayload(<string>(arrsize));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names6"
    }
    resource function keyNames6(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = arr.length();

        http:Response res = new;
        res.setTextPayload(<string>(arrsize));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/map"
    }
    resource function getmap(http:Caller caller, http:Request req) {
        http:Response res = new;
        http:Session session = req.createSessionIfAbsent();
        if (!req.hasHeader("counter")) {
            session.setAttribute("Name", "chamil");
            session.setAttribute("Lang", "ballerina");
            map<any> attributes = session.getAttributes();
            string[] arr = attributes.keys();
            string v0;
            v0 = <string>attributes[arr[0]];
            res.setTextPayload(arr[0] + ":" + v0);
        } else {
            map<any> attributes = session.getAttributes();
            string[] arr = attributes.keys();
            string v1;
            v1 = <string>attributes[arr[1]];
            res.setTextPayload(arr[1] + ":" + v1);
        }
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/map2"
    }
    resource function getmap2(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        map<any> attributes = session.getAttributes();
        string v0 = "map not present";
        if ((attributes.length()) != 0) {
            string[] arr = attributes.keys();
            v0 = <string>attributes[arr[0]];
        }
        http:Response res = new;
        res.setTextPayload("value" + ":" + v0);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/id1"
    }
    resource function id1(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        string id = session.getId();

        http:Response res = new;
        res.setTextPayload(id);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/id2"
    }
    resource function id2(http:Caller caller, http:Request req) {
        http:Session session = req.getSession();
        string id;
        if (session != null) {
            id = session.getId();
        }
        http:Response res = new;
        res.setTextPayload(id);
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new1"
    }
    resource function new1(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        boolean stat = session.isNew();

        http:Response res = new;
        res.setTextPayload(<string>(stat));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new2"
    }
    resource function new2(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        int time = session.getCreationTime();

        http:Response res = new;
        res.setTextPayload(<string>(time));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new3"
    }
    resource function new3(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        int time = session.getCreationTime();

        http:Response res = new;
        res.setTextPayload(<string>(time));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new4"
    }
    resource function new4(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        int time = session.getLastAccessedTime();

        http:Response res = new;
        res.setTextPayload(<string>(time));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new5"
    }
    resource function new5(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        int time = session.getLastAccessedTime();

        http:Response res = new;
        res.setTextPayload(<string>(time));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new6"
    }
    resource function new6(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(60);

        http:Response res = new;
        res.setTextPayload(<string>(time));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new7"
    }
    resource function new7(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        session.setMaxInactiveInterval(89);

        http:Response res = new;
        res.setTextPayload("done");
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new8"
    }
    resource function new8(http:Caller caller, http:Request req) {
        http:Session session = req.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(-1);

        http:Response res = new;
        res.setTextPayload(<string>(time));
        checkpanic caller->respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new9"
    }
    resource function new9(http:Caller caller, http:Request req) {
        string myName = "FirstName";
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName = <string>attribute;
        }
        Session.setAttribute("name", "SecondName");

        http:Response res = new;
        res.setTextPayload(myName);
        checkpanic caller->respond(res);
    }
}
