import ballerina.net.http;
import ballerina.lang.messages;
import ballerina.net.httpsession;

struct Data {
            string name;
        }

@http:BasePath {value:"/sample"}
service sample {
    @http:GET{}
    @http:Path{value:"/init"}
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
    @http:Path{value:"/initparam"}
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
    @http:Path{value:"/initparam3"}
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
    @http:Path{value:"/getAt"}
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

    @http:POST{}
    @http:Path{value:"/hello"}
    resource hello (message m) {

        string result = messages:getStringPayload (m);
        httpsession:Session session = httpsession:getSession(m);
        any attribute = httpsession:getAttribute(session,"name");
        if(attribute != null) {
            result = (string) attribute;
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
        httpsession:Session ses = httpsession:getSession(m);
        if(httpsession:getAttribute(ses,"Counter") == null) {
            sessionCounter = 0;
        } else {
            sessionCounter = (int) httpsession:getAttribute(ses,"Counter");
        }
        sessionCounter = sessionCounter+1;
        httpsession:setAttribute(ses, "Counter", sessionCounter);
        messages:setStringPayload(m, sessionCounter);
        reply m;
    }
}

service sample2 {
    @http:GET{}
    resource echoName (message m) {
        string myName = "wso2";
        httpsession:Session Session = httpsession:getSession(m);
        any attribute = httpsession:getAttribute(Session,"name");
        if(attribute != null) {
            myName = (string) attribute;
        }
        httpsession:setAttribute(Session, "name", "chamil");
        messages:setStringPayload(m, myName);
        reply m;
    }

    @http:POST{}
    resource myStruct (message m) {

        string result = messages:getStringPayload (m);
        Data d = {name:result};
        httpsession:Session Session = httpsession:getSession(m);
        any attribute = httpsession:getAttribute(Session, "nameStruct");
        if(attribute != null) {
            d = (Data) attribute;
        } else {
            httpsession:setAttribute(Session, "nameStruct", d);
        }

        messages:setStringPayload(m, d.name);
        reply m;
    }
}
