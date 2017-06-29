import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.net.httpsession;
import ballerina.lang.errors;
import ballerina.lang.strings;

struct Data {
            string name;
        }

@http:BasePath {value:"/sample"}
service sample {
    @http:GET{}
    @http:Path{value:"/test1"}
    resource echo (message m) {

        string result = "";
        httpsession:Session session = httpsession:getSession(m);
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
        httpsession:Session session = httpsession:getSessionWithParam(m,true);
        if (session != null) {
            result = "session created";
        }
        messages:setStringPayload(m, result);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/test3"}
    resource echo3 (message m) {

        string result = "";
        httpsession:Session session = httpsession:getSessionWithParam(m, false);
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
        httpsession:Session session = httpsession:getSession(m);
        any attribute = httpsession:getAttribute(session, "name");
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
        httpsession:Session session = httpsession:getSessionWithParam(m, false );
        any attribute = httpsession:getAttribute(session,"name");
        messages:setStringPayload(m, result);
        reply m;
    }

    @http:POST{}
    @http:Path{value:"/hello"}
    resource hello (message m) {

        errors:TypeCastError err;
        string result = messages:getStringPayload (m);
        httpsession:Session session = httpsession:getSession(m);
        any attribute = httpsession:getAttribute(session,"name");
        if(attribute != null) {
            result,err = (string) attribute;

        } else {
            httpsession:setAttribute(session, "name", result);
        }
        messages:setStringPayload(m, result);
        reply m;
    }
}

@http:BasePath {value:"/counter"}
service counter {
    @http:GET{}
    @http:Path{value:"/echo"}
    resource echoCount (message m) {

        int sessionCounter;
        errors:TypeCastError err;
        httpsession:Session ses = httpsession:getSession(m);
        if(httpsession:getAttribute(ses,"Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int) httpsession:getAttribute(ses,"Counter");
        }
        sessionCounter = sessionCounter+1;
        httpsession:setAttribute(ses, "Counter", sessionCounter);
        messages:setStringPayload(m, strings:valueOf(sessionCounter));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/echo2"}
    resource echoCount2 (message m) {

        int sessionCounter;
        errors:TypeCastError err;
        httpsession:Session ses = httpsession:getSessionWithParam(m, true);
        if(httpsession:getAttribute(ses,"Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter, err = (int) httpsession:getAttribute(ses,"Counter");
        }
        sessionCounter = sessionCounter+1;
        httpsession:setAttribute(ses, "Counter", sessionCounter);
        messages:setStringPayload(m, strings:valueOf(sessionCounter));
        reply m;
    }
}

service sample2 {
    @http:GET{}
    resource echoName (message m) {
        string myName = "wso2";
        errors:TypeCastError err;
        httpsession:Session Session = httpsession:getSession(m);
        any attribute = httpsession:getAttribute(Session,"name");
        if(attribute != null) {
            myName, err = (string) attribute;
        }
        httpsession:setAttribute(Session, "name", "chamil");
        messages:setStringPayload(m, myName);
        reply m;
    }

    @http:POST{}
    resource myStruct (message m) {

        string result = messages:getStringPayload (m);
        errors:TypeCastError err;
        Data d = {name:result};
        httpsession:Session Session = httpsession:getSession(m);
        any attribute = httpsession:getAttribute(Session, "nameStruct");
        if(attribute != null) {
            d, err = (Data) attribute;
        } else {
            httpsession:setAttribute(Session, "nameStruct", d);
        }
        messages:setStringPayload(m, d.name);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names"}
    resource keyNames (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        httpsession:setAttribute(ses, "Counter", "1");
        httpsession:setAttribute(ses, "Name", "chamil");
        string[] arr = httpsession:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, "arraysize:" + arrsize);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names2"}
    resource keyNames2 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        httpsession:setAttribute(ses, "Counter", "1");
        httpsession:setAttribute(ses, "location", "colombo");
        string[] arr = httpsession:getAttributeNames(ses);
        messages:setStringPayload(m, arr[0]);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names3"}
    resource keyNames3 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        httpsession:setAttribute(ses, "location", "colombo");
        httpsession:setAttribute(ses, "channel", "yue");
        httpsession:setAttribute(ses, "month", "june");
        httpsession:setAttribute(ses, "Name", "chamil");
        httpsession:removeAttribute(ses, "Name");
        string[] arr = httpsession:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, strings:valueOf(arrsize));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names4"}
    resource keyNames4 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        httpsession:setAttribute(ses, "Counter", "1");
        httpsession:setAttribute(ses, "Name", "chamil");
        httpsession:removeAttribute(ses, "Name");
        httpsession:invalidate(ses);
        string[] arr = httpsession:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, strings:valueOf(arrsize));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names5"}
    resource keyNames5 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        string[] arr = httpsession:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, strings:valueOf(arrsize));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/names6"}
    resource keyNames6 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        httpsession:setAttribute(ses, "location", "colombo");
        httpsession:removeAttribute(ses, "Name");
        string[] arr = httpsession:getAttributeNames(ses);
        int arrsize = arr.length;
        messages:setStringPayload(m, strings:valueOf(arrsize));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/id1"}
    resource id1 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        string id = httpsession:getId(ses);
        messages:setStringPayload(m, id);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/id2"}
    resource id2 (message m) {

        httpsession:Session ses = httpsession:getSessionWithParam(m,false);
        string id = httpsession:getId(ses);
        messages:setStringPayload(m, id);
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new1"}
    resource new1 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        boolean stat = httpsession:isNew(ses);
        messages:setStringPayload(m, strings:valueOf(stat));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new2"}
    resource new2 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        int time = httpsession:getCreationTime(ses);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new3"}
    resource new3 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        httpsession:invalidate(ses);
        int time = httpsession:getCreationTime(ses);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new4"}
    resource new4 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        int time = httpsession:getLastAccessedTime(ses);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new5"}
    resource new5 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        httpsession:invalidate(ses);
        int time = httpsession:getLastAccessedTime(ses);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new6"}
    resource new6 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        int time = httpsession:getMaxInactiveInterval(ses);
        httpsession:setMaxInactiveInterval(ses, 60);
        messages:setStringPayload(m, strings:valueOf(time));
        reply m;
    }

    @http:GET{}
    @http:Path{value:"/new7"}
    resource new7 (message m) {

        httpsession:Session ses = httpsession:getSession(m);
        httpsession:invalidate(ses);
        httpsession:setMaxInactiveInterval(ses, 89);
        messages:setStringPayload(m, "done");
        reply m;
    }

}
