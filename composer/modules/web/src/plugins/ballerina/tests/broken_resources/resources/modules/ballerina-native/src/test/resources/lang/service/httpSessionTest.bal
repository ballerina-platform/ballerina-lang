import ballerina/http;
import ballerina/lang.messages;
import ballerina/lang.errors;
import ballerina/lang.strings;

struct Data {
            string name;
        }

@http:configuration{basePath:"/sample"}
service<http> sample {
    @http:GET{}
    @http:Path{value:"/test1"}
    resource echo (message m) {

        string result = "";
        http:Session session = http:createSessionIfAbsent(m);
        if (session != null) {
            result = "session created";
        }
        messages:setStringPayload(m, result);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/test2"}
    resource echo2 (message m) {

        string result = "";
        http:Session session = http:getSession(m);
        if (session != null) {
            result = "session is returned";
        } else {
            result = "no session id available";
        }
        messages:setStringPayload(m, result);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/test3"}
    resource echo3 (message m) {

        string result = "";
        http:Session session = http:getSession(m);
        if (session != null) {
            result = "session created";
        } else {
            result = "session is not created";
        }
        messages:setStringPayload(m, result);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/test4"}
    resource testGetAt (message m) {

        string result = "";
        http:Session session = http:createSessionIfAbsent(m);
        any attribute = http:getAttribute(session, "name");
        if(attribute != null) {
            result = "attribute available";
        } else {
        result = "attribute not available";
        }
        messages:setStringPayload(m, result);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/test5"}
    resource echo5 (message m) {

        string result = "chamil";
        http:Session session = http:getSession(m);
        any attribute = http:getAttribute(session,"name");
        messages:setStringPayload(m, result);
        reply m;
    }

    @http:GET{}
        @http:Path{value:"/test6"}
        resource echo6 (message m) {

            string myName = "chamil";
            errors:TypeCastError err;
            http:Session session1 = http:createSessionIfAbsent(m);
            http:Session session2 = http:createSessionIfAbsent(m);
            http:setAttribute(session1,"name","wso2");
            any attribute = http:getAttribute(session2,"name");
            if(attribute != null) {
                myName, err = (string) attribute;
            }
            messages:setStringPayload(m, myName);
            reply m;
        }

    @http:POST{}
    @http:Path{value:"/hello"}
    resource hello (message m) {

        errors:TypeCastError err;
        string result = messages:getStringPayload (m);
        http:Session session = http:createSessionIfAbsent(m);
        any attribute = http:getAttribute(session,"name");
        if(attribute != null) {
            result,err = (string) attribute;

        } else {
            http:setAttribute(session, "name", result);
        }
        messages:setStringPayload(m, result);
        reply m;
    }
}

@http:configuration{basePath:"/counter"}
service<http> counter {
    @http:GET{}
    @http:Path{value:"/echo"}
    resource echoCount (message m) {

        int sessionCounter;
        errors:TypeCastError err;
        http:Session ses = http:createSessionIfAbsent(m);
        if(http:getAttribute(ses,"Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int) http:getAttribute(ses,"Counter");
        }
        sessionCounter = sessionCounter+1;
        http:setAttribute(ses, "Counter", sessionCounter);
        messages:setStringPayload(m, strings:valueOf(sessionCounter));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/echo2"}
    resource echoCount2 (message m) {

        int sessionCounter;
        errors:TypeCastError err;
        http:Session ses = http:getSession(m);
        if(http:getAttribute(ses,"Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int) http:getAttribute(ses,"Counter");
        }
        sessionCounter = sessionCounter+1;
        http:setAttribute(ses, "Counter", sessionCounter);
        messages:setStringPayload(m, strings:valueOf(sessionCounter));
        reply m;
    }
}

@http:configuration{basePath:"/sample2"}
service<http> sample2 {
    @http:GET{}
    resource echoName (message m) {
        string myName = "wso2";
        errors:TypeCastError err;
        http:Session Session = http:createSessionIfAbsent(m);
        any attribute = http:getAttribute(Session,"name");
        if(attribute != null) {
            myName, err = (string) attribute;
        }
        http:setAttribute(Session, "name", "chamil");
        messages:setStringPayload(m, myName);
        reply m;
    }

    @http:POST{}
    resource myStruct (message m) {

        string result = messages:getStringPayload (m);
        errors:TypeCastError err;
        Data d = {name:result};
        http:Session Session = http:createSessionIfAbsent(m);
        any attribute = http:getAttribute(Session, "nameStruct");
        if(attribute != null) {
            d, err = (Data) attribute;
        } else {
            http:setAttribute(Session, "nameStruct", d);
        }
        messages:setStringPayload(m, d.name);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names"}
    resource keyNames (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        http:setAttribute(ses, "Counter", "1");
        http:setAttribute(ses, "Name", "chamil");
        string[] arr = http:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, "arraysize:" + arrsize);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names2"}
    resource keyNames2 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        http:setAttribute(ses, "Counter", "1");
        http:setAttribute(ses, "location", "colombo");
        string[] arr = http:getAttributeNames(ses);
        messages:setStringPayload(m, arr[0]);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names3"}
    resource keyNames3 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        http:setAttribute(ses, "location", "colombo");
        http:setAttribute(ses, "channel", "yue");
        http:setAttribute(ses, "month", "june");
        http:setAttribute(ses, "Name", "chamil");
        http:removeAttribute(ses, "Name");
        string[] arr = http:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, strings:valueOf(arrsize));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names4"}
    resource keyNames4 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        http:setAttribute(ses, "Counter", "1");
        http:setAttribute(ses, "Name", "chamil");
        http:removeAttribute(ses, "Name");
        http:invalidate(ses);
        string[] arr = http:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, strings:valueOf(arrsize));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names5"}
    resource keyNames5 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        string[] arr = http:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, strings:valueOf(arrsize));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names6"}
    resource keyNames6 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        http:setAttribute(ses, "location", "colombo");
        http:removeAttribute(ses, "Name");
        string[] arr = http:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, strings:valueOf(arrsize));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/id1"}
    resource id1 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        string id = http:getId(ses);
        messages:setStringPayload(m, id);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/id2"}
    resource id2 (message m) {

        http:Session ses = http:getSession(m);
        string id = http:getId(ses);
        messages:setStringPayload(m, id);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new1"}
    resource new1 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        boolean stat = http:isNew(ses);
        messages:setStringPayload(m, strings:valueOf(stat));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new2"}
    resource new2 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        int time = http:getCreationTime(ses);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new3"}
    resource new3 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        http:invalidate(ses);
        int time = http:getCreationTime(ses);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new4"}
    resource new4 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        int time = http:getLastAccessedTime(ses);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new5"}
    resource new5 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        http:invalidate(ses);
        int time = http:getLastAccessedTime(ses);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new6"}
    resource new6 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        int time = http:getMaxInactiveInterval(ses);
        http:setMaxInactiveInterval(ses, 60);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new7"}
    resource new7 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        http:invalidate(ses);
        http:setMaxInactiveInterval(ses, 89);
        messages:setStringPayload(m, "done");
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new8"}
    resource new8 (message m) {

        http:Session ses = http:createSessionIfAbsent(m);
        int time = http:getMaxInactiveInterval(ses);
        http:setMaxInactiveInterval(ses, -1);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }
}
