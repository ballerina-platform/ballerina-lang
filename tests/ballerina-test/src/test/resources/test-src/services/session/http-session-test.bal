import ballerina/http;
import ballerina/mime;
import ballerina/io;

type Data {
    string name,
};

endpoint http:NonListener sessionEP {
    port:9090
};

@http:ServiceConfig {basePath:"/sample"}
service<http:Service> sample bind sessionEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test1"
    }
    echo (endpoint conn, http:Request req) {

        string result = "";
        http:Session session = req.createSessionIfAbsent();
        if (session != null) {
            result = "session created";
        }
        http:Response res = new;
        res.setTextPayload(result);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test2"
    }
    echo2 (endpoint conn, http:Request req) {

        string result = "";
        http:Session session = req.getSession();
        if (session != null) {
            result = "session is returned";
        } else {
            result = "no session id available";
        }
        http:Response res = new;
        res.setTextPayload(result);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test3"
    }
    echo3 (endpoint conn, http:Request req) {

        string result = "";
        http:Session session = req.getSession();
        if (session != null) {
            result = "session created";
        } else {
            result = "session is not created";
        }
        http:Response res = new;
        res.setTextPayload(result);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test4"
    }
    testGetAt (endpoint conn, http:Request req) {

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
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test5"
    }
    echo5 (endpoint conn, http:Request req) {

        http:Response res = new;
        string result = "testValue";
        http:Session session = req.getSession();
        if (session != null) {
            any attribute = session.getAttribute("name");
            res.setTextPayload(<string>attribute);
        } else {
            res.setTextPayload(result);
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test6"
    }
    echo6 (endpoint conn, http:Request req) {

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
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/hello"
    }
    hello (endpoint conn, http:Request req) {
        http:Response res = new;
        string result;
        match req.getTextPayload() {
            error err => res.setTextPayload("Error");
            string textPayload => {
                result = textPayload;
                http:Session session = req.createSessionIfAbsent();
                any attribute = session.getAttribute("name");
                if (attribute != null) {
                    result = <string>attribute;
                } else {
                    session.setAttribute("name", result);
                }
                res.setTextPayload(result);
            }
        }
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath:"/counter"}
service<http:Service> counter bind sessionEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo"
    }
    echoCount (endpoint conn, http:Request req) {

        int sessionCounter;
        http:Session session = req.createSessionIfAbsent();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            var result = <int>session.getAttribute("Counter");
            match result {
                int value => sessionCounter = value;
                error err => io:println("Error occurred during int to string conversion");
            }
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);

        http:Response res = new;
        res.setTextPayload(<string> sessionCounter);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    echoCount2 (endpoint conn, http:Request req) {

        int sessionCounter;
        http:Session session = req.getSession();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            var result = <int>session.getAttribute("Counter");
            match result {
                int value => sessionCounter = value;
                error err => io:println("Error occurred during int to string conversion");
            }
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);

        http:Response res = new;
        res.setTextPayload(<string>(sessionCounter));
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath:"/sample2"}
service<http:Service> sample2 bind sessionEP {
    @http:ResourceConfig {
        methods:["GET"]
    }
    echoName (endpoint conn, http:Request req) {
        string myName = "wso2";
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName = <string>attribute;
        }
        Session.setAttribute("name", "chamil");

        http:Response res = new;
        res.setTextPayload(myName);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"]
    }
    myStruct (endpoint conn, http:Request req) {
        http:Response res = new;
        match req.getTextPayload() {
            error err => res.setTextPayload("Error");
            string payload => {
                Data d = {name:payload};
                http:Session Session = req.createSessionIfAbsent();
                any attribute = Session.getAttribute("nameStruct");
                if (attribute != null) {
                    var result = <Data>attribute;
                    match result {
                        Data returnData => d = returnData;
                        error err => io:println("Error occurred!");
                    }
                } else {
                    Session.setAttribute("nameStruct", d);
                }
                res.setTextPayload(d.name);
            }
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names"
    }
    keyNames (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = new;
        res.setTextPayload("arraysize:" + arrsize);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names2"
    }
    keyNames2 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("location", "colombo");
        string[] arr = session.getAttributeNames();

        http:Response res = new;
        res.setTextPayload(arr[0]);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names3"
    }
    keyNames3 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.setAttribute("channel", "yue");
        session.setAttribute("month", "june");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = new;
        res.setTextPayload(<string>(arrsize));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names4"
    }
    keyNames4 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        session.invalidate();
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = new;
        res.setTextPayload(<string>(arrsize));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names5"
    }
    keyNames5 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = new;
        res.setTextPayload(<string>(arrsize));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names6"
    }
    keyNames6 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = new;
        res.setTextPayload(<string>(arrsize));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/map"
    }
    getmap (endpoint conn, http:Request req) {

        http:Response res = new;
        http:Session session = req.createSessionIfAbsent();
        if (!req.hasHeader("counter")) {
            session.setAttribute("Name", "chamil");
            session.setAttribute("Lang", "ballerina");
            map attributes = session.getAttributes();
            string[] arr = attributes.keys();
            string v0;
            v0 = <string>attributes[arr[0]];
            res.setTextPayload(arr[0] + ":" + v0);
        } else {
            map attributes = session.getAttributes();
            string[] arr = attributes.keys();
            string v1;
            v1 = <string>attributes[arr[1]];
            res.setTextPayload(arr[1] + ":" + v1);
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/map2"
    }
    getmap2 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        map attributes = session.getAttributes();
        string v0 = "map not present";
        if ((lengthof attributes) != 0) {
            string[] arr = attributes.keys();
            v0 = <string>attributes[arr[0]];
        }
        http:Response res = new;
        res.setTextPayload("value" + ":" + v0);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/id1"
    }
    id1 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        string id = session.getId();

        http:Response res = new;
        res.setTextPayload(id);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/id2"
    }
    id2 (endpoint conn, http:Request req) {

        http:Session session = req.getSession();
        string id;
        if (session != null) {
            id = session.getId();
        }
        http:Response res = new;
        res.setTextPayload(id);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new1"
    }
    new1 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        boolean stat = session.isNew();

        http:Response res = new;
        res.setTextPayload(<string>(stat));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new2"
    }
    new2 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getCreationTime();

        http:Response res = new;
        res.setTextPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new3"
    }
    new3 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        int time = session.getCreationTime();

        http:Response res = new;
        res.setTextPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new4"
    }
    new4 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getLastAccessedTime();

        http:Response res = new;
        res.setTextPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new5"
    }
    new5 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        int time = session.getLastAccessedTime();

        http:Response res = new;
        res.setTextPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new6"
    }
    new6 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(60);

        http:Response res = new;
        res.setTextPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new7"
    }
    new7 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        session.setMaxInactiveInterval(89);

        http:Response res = new;
        res.setTextPayload("done");
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new8"
    }
    new8 (endpoint conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(-1);

        http:Response res = new;
        res.setTextPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new9"
    }
    new9 (endpoint conn, http:Request req) {
        string myName = "FirstName";
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName = <string>attribute;
        }
        Session.setAttribute("name", "SecondName");

        http:Response res = new;
        res.setTextPayload(myName);
        _ = conn -> respond(res);
    }
}
