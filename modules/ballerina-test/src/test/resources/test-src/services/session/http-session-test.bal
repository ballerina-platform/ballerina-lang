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
    resource echo (http:Request req, http:Response res) {

        string result = "";
        http:Session session = req.createSessionIfAbsent();
        if (session != null) {
            result = "session created";
        }
        res.setStringPayload(result);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test2"
    }
    resource echo2 (http:Request req, http:Response res) {

        string result = "";
        http:Session session = req.getSession();
        if (session != null) {
            result = "session is returned";
        } else {
            result = "no session id available";
        }
        res.setStringPayload(result);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test3"
    }
    resource echo3 (http:Request req, http:Response res) {

        string result = "";
        http:Session session = req.getSession();
        if (session != null) {
            result = "session created";
        } else {
            result = "session is not created";
        }
        res.setStringPayload(result);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test4"
    }
    resource testGetAt (http:Request req, http:Response res) {

        string result = "";
        http:Session session = req.createSessionIfAbsent();
        any attribute = session.getAttribute("name");
        if (attribute != null) {
            result = "attribute available";
        } else {
            result = "attribute not available";
        }
        res.setStringPayload(result);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test5"
    }
    resource echo5 (http:Request req, http:Response res) {

        string result = "chamil";
        http:Session session = req.getSession();
        any attribute = session.getAttribute("name");
        res.setStringPayload(result);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/test6"
    }
    resource echo6 (http:Request req, http:Response res) {

        string myName = "chamil";
        TypeCastError err;
        http:Session session1 = req.createSessionIfAbsent();
        http:Session session2 = req.createSessionIfAbsent();
        session1.setAttribute("name", "wso2");
        any attribute = session2.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        res.setStringPayload(myName);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/hello"
    }
    resource hello (http:Request req, http:Response res) {

        TypeCastError err;
        string result = req.getStringPayload();
        http:Session session = req.createSessionIfAbsent();
        any attribute = session.getAttribute("name");
        if (attribute != null) {
            result, err = (string)attribute;
        } else {
            session.setAttribute("name", result);
        }
        res.setStringPayload(result);
        _ = res.send();
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
        http:Session session = req.createSessionIfAbsent();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int)session.getAttribute("Counter");
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);
        res.setStringPayload(<string> sessionCounter);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/echo2"
    }
    resource echoCount2 (http:Request req, http:Response res) {

        int sessionCounter;
        TypeCastError err;
        http:Session session = req.getSession();
        if (session.getAttribute("Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int)session.getAttribute("Counter");
        }
        sessionCounter = sessionCounter + 1;
        session.setAttribute("Counter", sessionCounter);
        res.setStringPayload(<string>(sessionCounter));
        _ = res.send();
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
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        Session.setAttribute("name", "chamil");
        res.setStringPayload(myName);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["POST"]
    }
    resource myStruct (http:Request req, http:Response res) {

        string result = req.getStringPayload();
        TypeCastError err;
        Data d = {name:result};
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("nameStruct");
        if (attribute != null) {
            d, err = (Data)attribute;
        } else {
            Session.setAttribute("nameStruct", d);
        }
        res.setStringPayload(d.name);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names"
    }
    resource keyNames (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;
        res.setStringPayload("arraysize:" + arrsize);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names2"
    }
    resource keyNames2 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("location", "colombo");
        string[] arr = session.getAttributeNames();
        res.setStringPayload(arr[0]);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names3"
    }
    resource keyNames3 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.setAttribute("channel", "yue");
        session.setAttribute("month", "june");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;
        res.setStringPayload(<string>(arrsize));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names4"
    }
    resource keyNames4 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("Counter", "1");
        session.setAttribute("Name", "chamil");
        session.removeAttribute("Name");
        session.invalidate();
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;
        res.setStringPayload(<string>(arrsize));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names5"
    }
    resource keyNames5 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;
        res.setStringPayload(<string>(arrsize));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/names6"
    }
    resource keyNames6 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        session.setAttribute("location", "colombo");
        session.removeAttribute("Name");
        string[] arr = session.getAttributeNames();
        int arrsize = lengthof arr;
        res.setStringPayload(<string>(arrsize));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/map"
    }
    resource getmap (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        var value = req.getHeader("counter");
        if (value == null) {
            println("null header");
            session.setAttribute("Name", "chamil");
            session.setAttribute("Lang", "ballerina");
            map attributes = session.getAttributes();
            string[] arr = attributes.keys();
            string v0;
            v0,_ = (string)attributes[arr[0]];
            res.setStringPayload(arr[0] + ":" + v0);
        } else {
            map attributes = session.getAttributes();
            println(attributes);
            string[] arr = attributes.keys();
            string v1;
            v1,_ = (string)attributes[arr[1]];
            res.setStringPayload(arr[1] + ":" + v1);
        }
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/map2"
    }
    resource getmap2 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        map attributes = session.getAttributes();
        string v0 = "map not present";
        if ((lengthof attributes) != 0) {
            string[] arr = attributes.keys();
            v0,_ = (string)attributes[arr[0]];
        }
        res.setStringPayload("value" + ":" + v0);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/id1"
    }
    resource id1 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        string id = session.getId();
        res.setStringPayload(id);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/id2"
    }
    resource id2 (http:Request req, http:Response res) {

        http:Session session = req.getSession();
        string id = session.getId();
        res.setStringPayload(id);
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new1"
    }
    resource new1 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        boolean stat = session.isNew();
        res.setStringPayload(<string>(stat));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new2"
    }
    resource new2 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getCreationTime();
        res.setStringPayload(<string>(time));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new3"
    }
    resource new3 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        int time = session.getCreationTime();
        res.setStringPayload(<string>(time));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new4"
    }
    resource new4 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getLastAccessedTime();
        res.setStringPayload(<string>(time));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new5"
    }
    resource new5 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        int time = session.getLastAccessedTime();
        res.setStringPayload(<string>(time));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new6"
    }
    resource new6 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(60);
        res.setStringPayload(<string>(time));
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new7"
    }
    resource new7 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        session.invalidate();
        session.setMaxInactiveInterval(89);
        res.setStringPayload("done");
        _ = res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/new8"
    }
    resource new8 (http:Request req, http:Response res) {

        http:Session session = req.createSessionIfAbsent();
        int time = session.getMaxInactiveInterval();
        session.setMaxInactiveInterval(-1);
        res.setStringPayload(<string>(time));
        _ = res.send();
    }

    resource new9 (http:Request req, http:Response res) {
        string myName = "FirstName";
        TypeCastError err;
        http:Session Session = req.createSessionIfAbsent();
        any attribute = Session.getAttribute("name");
        if (attribute != null) {
            myName, err = (string)attribute;
        }
        Session.setAttribute("name", "SecondName");
        res.setStringPayload(myName);
        _ = res.send();
    }
}
