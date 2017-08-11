import ballerina.lang.messages;
import ballerina.lang.strings;

const string myConst = "MyParam1";

connector TestConnector(string param1, string param2, int param3) {

    EchoConnector echoConnector = create EchoConnector(param1);

    boolean action2Invoked;

    action action1() (boolean){
        return action2Invoked;
    }

    action action2() {
        action2Invoked = true;
    }

    action action3() (string) {
        return param1;
    }

    action action4(string actionParam) (string, string, int) {
        return actionParam, param2, param3;
    }

    action action5(string actionParam) (string) {
        string s;
        s = EchoConnector.echoAction(echoConnector, actionParam);
        return s;
    }

    action action6(string echoConnectorParam, string actionParam) (string) {
        EchoConnector localEchoConnector = create EchoConnector(echoConnectorParam);
        string s;

        s =  EchoConnector.echoAction(localEchoConnector, actionParam);
        return s;
    }
}

connector EchoConnector(string greeting) {

    action echoAction(string name) (string) {
        return greeting + ", " + name;
    }

}

@BasePath {value:"/invoke"}
service actionInvokeService {

    TestConnector testConnector = create TestConnector(myConst, "MyParam2", 5);

    @GET{}
    @Path {value:"/action3"}
    resource action3Resource (message m) {

        string actionResponse;

        actionResponse = testConnector.action3();
        message response = {};
        messages:setStringPayload(response, actionResponse);
        reply response;
    }


    @GET{}
    @Path {value:"/action1"}
    resource action1Resource (message m) {

        boolean actionResponse;

        actionResponse = testConnector.action1();
        message response = {};
        messages:setStringPayload(response, strings:valueOf(actionResponse));
        reply response;
    }


    @GET{}
    @Path {value:"/action2"}
    resource action2Resource (message m) {

        testConnector.action2();
        reply m;
    }

    @GET{}
    @Path {value:"/action5"}
    resource action5Resource (message m) {

        string actionResponse;

        actionResponse = testConnector.action5(myConst);
        message response = {};
        messages:setStringPayload(response, actionResponse);
        reply response;
    }

    @GET{}
    @Path {value:"/action6"}
    resource action6Resource (message m) {

        string actionResponse;

        actionResponse = testConnector.action6("Hello", "World");
        message response = {};
        messages:setStringPayload(response, actionResponse);
        reply response;
    }
}