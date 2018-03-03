import ballerina.net.http;

struct Data {
    string name;
}

@http:configuration {basePath:"/sample"}
service<http> sample {
    @http:resourceConfig {
        methods:["GET"],
        path:"/test1"
    }
    resource echo (http:Connection conn, http:InRequest req) {

        string result = "";
        http:Session session = conn.createSessionIfAbsent();
        if (session != null) {
            result = "session created";
        }
        http:OutResponse res = {};
        res.setStringPayload(result);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test2"
    }
    resource echo2 (http:Connection conn, http:InRequest req) {

        string result = "";
        http:Session session = conn.getSession();
        if (session != null) {
            result = "session is returned";
        } else {
            result = "no session id available";
        }
        http:OutResponse res = {};
        res.setStringPayload(result);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test3"
    }
    resource echo3 (http:Connection conn, http:InRequest req) {

        string result = "";
        http:Session session = conn.getSession();
        if (session != null) {
            result = "session created";
        } else {
            result = "session is not created";
        }
        http:OutResponse res = {};
        res.setStringPayload(result);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test4"
    }
    resource testGetAt (http:Connection conn, http:InRequest req) {

        string result = "";
        http:Session session = conn.createSessionIfAbsent();
        any attribute = session.getAttribute("name");
        if (attribute != null) {
            result = "attribute available";
        } else {
            result = "attribute not available";
        }
        http:OutResponse res = {};
        res.setStringPayload(result);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test5"
    }
    resource echo5 (http:Connection conn, http:InRequest req) {

        http:OutResponse res = {};
        string result = "chamil";
        http:Session session = conn.getSession();
        any attribute = session.getAttribute("name");
        res.setStringPayload(result);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test6"
    }
    resource echo6 (http:Connection conn, http:InRequest req) {

        string myName = "chamil";
        error err;
        http:Session session1 = conn.createSessionIfAbsent();
        http:Session session2 = conn.createSessionIfAbsent();
        session1.setAttribute("name", "wso2");
        any attribute = session2.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        http:OutResponse res = {};
        res.setStringPayload(myName);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/hello"
    }
    resource hello (http:Connection conn, http:InRequest req) {

        error err;
        string result = req.getStringPayload();
        http:Session session = conn.createSessionIfAbsent();
        any attribute = session.getAttribute("name");
        if (attribute != null) {
            result, err = (string)attribute;
        } else {
            session.setAttribute("name", result);
        }
        http:OutResponse res = {};
        res.setStringPayload(result);
        _ = conn.respond(res);
    }
}

@http:configuration {basePath:"/counter"}
service<http> counter {
    @http:resourceConfig {
        methods:["GET"],
        path:"/echo"
    }
    resource echoCount (http:Connection conn, http:InRequest req) {

        int sessionCounter;
        error err;
        http:Session session = conn.createSessionIfAbsent();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int)session.getAttribute("Counter");
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);

        http:OutResponse res = {};
        res.setStringPayload(<string> sessionCounter);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    resource echoCount2 (http:Connection conn, http:InRequest req) {

        int sessionCounter;
        error err;
        http:Session session = conn.getSession();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int)session.getAttribute("Counter");
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);

        http:OutResponse res = {};
        res.setStringPayload(<string>(sessionCounter));
        _ = conn.respond(res);
    }
}

@http:configuration {basePath:"/sample2"}
service<http> sample2 {
    @http:resourceConfig {
        methods:["GET"]
    }
    resource echoName (http:Connection conn, http:InRequest req) {
        string myName = "wso2";
        error err;
        http:Session Session = conn.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        Session.setAttribute("name", "chamil");

        http:OutResponse res = {};
        res.setStringPayload(myName);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource myStruct (http:Connection conn, http:InRequest req) {

        string result = req.getStringPayload();
        error err;
        Data d = {name:result};
        http:Session Session = conn.createSessionIfAbsent();
        any attribute = Session.getAttribute("nameStruct");
        if (attribute != null) {
            d, err = (Data)attribute;
        } else {
            Session.setAttribute("nameStruct", d);
        }
        http:OutResponse res = {};
        res.setStringPayload(d.name);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names"
    }
    resource keyNames (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:OutResponse res = {};
        res.setStringPayload("arraysize:" + arrsize);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names2"
    }
    resource keyNames2 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("location", "colombo");
        string[] arr = session.getAttributeNames();

        http:OutResponse res = {};
        res.setStringPayload(arr[0]);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names3"
    }
    resource keyNames3 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.setAttribute("channel", "yue");
        session.setAttribute("month", "june");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:OutResponse res = {};
        res.setStringPayload(<string>(arrsize));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names4"
    }
    resource keyNames4 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        session.invalidate();
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:OutResponse res = {};
        res.setStringPayload(<string>(arrsize));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names5"
    }
    resource keyNames5 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:OutResponse res = {};
        res.setStringPayload(<string>(arrsize));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names6"
    }
    resource keyNames6 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;

        http:OutResponse res = {};
        res.setStringPayload(<string>(arrsize));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/map"
    }
    resource getmap (http:Connection conn, http:InRequest req) {

        http:OutResponse res = {};
        http:Session session = conn.createSessionIfAbsent();
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
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/map2"
    }
    resource getmap2 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        map attributes = session.getAttributes();
        string v0 = "map not present";
        if ((lengthof attributes) != 0) {
            string[] arr = attributes.keys();
            v0,_ = (string)attributes[arr[0]];
        }
        http:OutResponse res = {};
        res.setStringPayload("value" + ":" + v0);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/id1"
    }
    resource id1 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        string id = session.getId();

        http:OutResponse res = {};
        res.setStringPayload(id);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/id2"
    }
    resource id2 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.getSession();
        string id = session.getId();

        http:OutResponse res = {};
        res.setStringPayload(id);
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new1"
    }
    resource new1 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        boolean stat = session.isNew();

        http:OutResponse res = {};
        res.setStringPayload(<string>(stat));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new2"
    }
    resource new2 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        int time = session.getCreationTime();

        http:OutResponse res = {};
        res.setStringPayload(<string>(time));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new3"
    }
    resource new3 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        session.invalidate();
        int time = session.getCreationTime();

        http:OutResponse res = {};
        res.setStringPayload(<string>(time));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new4"
    }
    resource new4 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        int time = session.getLastAccessedTime();

        http:OutResponse res = {};
        res.setStringPayload(<string>(time));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new5"
    }
    resource new5 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        session.invalidate();
        int time = session.getLastAccessedTime();

        http:OutResponse res = {};
        res.setStringPayload(<string>(time));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new6"
    }
    resource new6 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(60);

        http:OutResponse res = {};
        res.setStringPayload(<string>(time));
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new7"
    }
    resource new7 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        session.invalidate();
        session.setMaxInactiveInterval(89);

        http:OutResponse res = {};
        res.setStringPayload("done");
        _ = conn.respond(res);
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new8"
    }
    resource new8 (http:Connection conn, http:InRequest req) {

        http:Session session = conn.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(-1);

        http:OutResponse res = {};
        res.setStringPayload(<string>(time));
        _ = conn.respond(res);
    }

    resource new9 (http:Connection conn, http:InRequest req) {
        string myName = "FirstName";
        error err;
        http:Session Session = conn.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        Session.setAttribute("name", "SecondName");

        http:OutResponse res = {};
        res.setStringPayload(myName);
        _ = conn.respond(res);
    }
}
