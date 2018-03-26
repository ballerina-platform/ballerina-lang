import ballerina/net.http;
import ballerina/net.http.mock;

struct Data {
    string name;
}

endpoint<mock:NonListeningService> sessionEP {
    port:9090
}

@http:ServiceConfig {basePath:"/sample", endpoints:[sessionEP]}
service<http:Service> sample {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/test1"
    }
    resource echo (http:ServerConnector conn, http:Request req) {

        string result = "";
        http:Session session = req.createSessionIfAbsent();
        if (session != null) {
            result = "session created";
        }
        http:Response res = {};
        res.setStringPayload(result);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test2"
    }
    resource echo2 (http:ServerConnector conn, http:Request req) {

        string result = "";
        http:Session session = req.getSession();
        if (session != null) {
            result = "session is returned";
        } else {
            result = "no session id available";
        }
        http:Response res = {};
        res.setStringPayload(result);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test3"
    }
    resource echo3 (http:ServerConnector conn, http:Request req) {

        string result = "";
        http:Session session = req.getSession();
        if (session != null) {
            result = "session created";
        } else {
            result = "session is not created";
        }
        http:Response res = {};
        res.setStringPayload(result);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test4"
    }
    resource testGetAt (http:ServerConnector conn, http:Request req) {

        string result = "";
        http:Session session = req.createSessionIfAbsent();
        any attribute = session.getAttribute("name");
        if (attribute != null) {
            result = "attribute available";
        } else {
            result = "attribute not available";
        }
        http:Response res = {};
        res.setStringPayload(result);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test5"
    }
    resource echo5 (http:ServerConnector conn, http:Request req) {

        http:Response res = {};
        string result = "chamil";
        http:Session session = req.getSession();
        any attribute = session.getAttribute("name");
        res.setStringPayload(result);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/test6"
    }
    resource echo6 (http:ServerConnector conn, http:Request req) {

        string myName = "chamil";
        error err;
        http:Session session1 = req.createSessionIfAbsent();
        http:Session session2 = req.createSessionIfAbsent();
        session1.setAttribute("name", "wso2");
        any attribute = session2.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        http:Response res = {};
        res.setStringPayload(myName);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"],
        path:"/hello"
    }
    resource hello (http:ServerConnector conn, http:Request req) {

        error err;
        var result, _ = req.getStringPayload();
        http:Session session = req.createSessionIfAbsent();
        any attribute = session.getAttribute("name");
        if (attribute != null) {
            result, err = (string)attribute;
        } else {
            session.setAttribute("name", result);
        }
        http:Response res = {};
        res.setStringPayload(result);
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath:"/counter", endpoints:[sessionEP]}
service<http:Service> counter {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo"
    }
    resource echoCount (http:ServerConnector conn, http:Request req) {

        int sessionCounter;
        error err;
        http:Session session = req.createSessionIfAbsent();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int)session.getAttribute("Counter");
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);

        http:Response res = {};
        res.setStringPayload(<string> sessionCounter);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    resource echoCount2 (http:ServerConnector conn, http:Request req) {

        int sessionCounter;
        error err;
        http:Session session = req.getSession();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int)session.getAttribute("Counter");
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);

        http:Response res = {};
        res.setStringPayload(<string>(sessionCounter));
        _ = conn -> respond(res);
    }
}

@http:ServiceConfig {basePath:"/sample2", endpoints:[sessionEP]}
service<http:Service> sample2 {
    @http:ResourceConfig {
        methods:["GET"]
    }
    resource echoName (http:ServerConnector conn, http:Request req) {
        string myName = "wso2";
        error err;
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        Session.setAttribute("name", "chamil");

        http:Response res = {};
        res.setStringPayload(myName);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["POST"]
    }
    resource myStruct (http:ServerConnector conn, http:Request req) {

        var result, _ = req.getStringPayload();
        error err;
        Data d = {name:result};
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("nameStruct");
        if (attribute != null) {
            d, err = (Data)attribute;
        } else {
            Session.setAttribute("nameStruct", d);
        }
        http:Response res = {};
        res.setStringPayload(d.name);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names"
    }
    resource keyNames (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = {};
        res.setStringPayload("arraysize:" + arrsize);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names2"
    }
    resource keyNames2 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("location", "colombo");
        string[] arr = session.getAttributeNames();

        http:Response res = {};
        res.setStringPayload(arr[0]);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names3"
    }
    resource keyNames3 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.setAttribute("channel", "yue");
        session.setAttribute("month", "june");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = {};
        res.setStringPayload(<string>(arrsize));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names4"
    }
    resource keyNames4 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        session.invalidate();
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = {};
        res.setStringPayload(<string>(arrsize));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names5"
    }
    resource keyNames5 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = {};
        res.setStringPayload(<string>(arrsize));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/names6"
    }
    resource keyNames6 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:Response res = {};
        res.setStringPayload(<string>(arrsize));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/map"
    }
    resource getmap (http:ServerConnector conn, http:Request req) {

        http:Response res = {};
        http:Session session = req.createSessionIfAbsent();
        var value = req.getHeader("counter");
        if (value == null) {
            session.setAttribute("Name", "chamil");
            session.setAttribute("Lang", "ballerina");
            map attributes = session.getAttributes();
            string[] arr = attributes.keys();
            string v0;
            v0,_ = (string)attributes[arr[0]];
            res.setStringPayload(arr[0] + ":" + v0);
        } else {
            map attributes = session.getAttributes();
            string[] arr = attributes.keys();
            string v1;
            v1,_ = (string)attributes[arr[1]];
            res.setStringPayload(arr[1] + ":" + v1);
        }
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/map2"
    }
    resource getmap2 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        map attributes = session.getAttributes();
        string v0 = "map not present";
        if ((lengthof attributes) != 0) {
            string[] arr = attributes.keys();
            v0,_ = (string)attributes[arr[0]];
        }
        http:Response res = {};
        res.setStringPayload("value" + ":" + v0);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/id1"
    }
    resource id1 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        string id = session.getId();

        http:Response res = {};
        res.setStringPayload(id);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/id2"
    }
    resource id2 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.getSession();
        string id = session.getId();

        http:Response res = {};
        res.setStringPayload(id);
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new1"
    }
    resource new1 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        boolean stat = session.isNew();

        http:Response res = {};
        res.setStringPayload(<string>(stat));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new2"
    }
    resource new2 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getCreationTime();

        http:Response res = {};
        res.setStringPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new3"
    }
    resource new3 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        int time = session.getCreationTime();

        http:Response res = {};
        res.setStringPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new4"
    }
    resource new4 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getLastAccessedTime();

        http:Response res = {};
        res.setStringPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new5"
    }
    resource new5 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        int time = session.getLastAccessedTime();

        http:Response res = {};
        res.setStringPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new6"
    }
    resource new6 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(60);

        http:Response res = {};
        res.setStringPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new7"
    }
    resource new7 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        session.setMaxInactiveInterval(89);

        http:Response res = {};
        res.setStringPayload("done");
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new8"
    }
    resource new8 (http:ServerConnector conn, http:Request req) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(-1);

        http:Response res = {};
        res.setStringPayload(<string>(time));
        _ = conn -> respond(res);
    }

    @http:ResourceConfig {
        methods:["GET"],
        path:"/new9"
    }
    resource new9 (http:ServerConnector conn, http:Request req) {
        string myName = "FirstName";
        error err;
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        Session.setAttribute("name", "SecondName");

        http:Response res = {};
        res.setStringPayload(myName);
        _ = conn -> respond(res);
    }
}
