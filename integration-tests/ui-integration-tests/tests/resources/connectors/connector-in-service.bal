package samples.connectors.test;

import ballerina.lang.messages;
import ballerina.lang.strings;

const string myConst = "MyParam1";

connector TestConnector(string param1, string param2, int param3) {

    EchoConnector echoConnector = create EchoConnector(param1);

    boolean action2Invoked;

    action action1(TestConnector testConnector) (boolean){
        return action2Invoked;
    }

    action action2(TestConnector testConnector) {
        action2Invoked = true;
    }

    action action3(TestConnector testConnector) (string) {
        return param1;
    }

    action action4(TestConnector testConnector, string actionParam) (string, string, int) {
        return actionParam, param2, param3;
    }

    action action5(TestConnector testConnector, string actionParam) (string) {
        string s;
        s = EchoConnector.echoAction(echoConnector, actionParam);
        return s;
    }

    action action6(TestConnector testConnector, string echoConnectorParam, string actionParam) (string) {
        EchoConnector localEchoConnector = create EchoConnector(echoConnectorParam);
        string s;

        s =  EchoConnector.echoAction(localEchoConnector, actionParam);
        return s;
    }
}

connector EchoConnector(string greeting) {

    action echoAction(EchoConnector echoConnector, string name) (string) {
        return greeting + ", " + name;
    }

}

@BasePath ("/invoke")
service actionInvokeService {

    TestConnector testConnector = create TestConnector(myConst, "MyParam2", 5);

    @GET
    @Path ("/action3")
    resource action3Resource (message m) {

        string actionResponse;

        actionResponse = TestConnector.action3(testConnector);
        message response = {};
        messages:setStringPayload(response, actionResponse);
        reply response;
    }


    @GET
    @Path ("/action1")
    resource action1Resource (message m) {

        boolean actionResponse;

        actionResponse = TestConnector.action1(testConnector);
        message response = {};
        messages:setStringPayload(response, strings:valueOf(actionResponse));
        reply response;
    }


    @GET
    @Path ("/action2")
    resource action2Resource (message m) {

        TestConnector.action2(testConnector);
        reply m;
    }

    @GET
    @Path ("/action5")
    resource action5Resource (message m) {

        string actionResponse;

        actionResponse = TestConnector.action5(testConnector, myConst);
        message response = {};
        messages:setStringPayload(response, actionResponse);
        reply response;
    }

    @GET
    @Path ("/action6")
    resource action6Resource (message m) {

        string actionResponse;

        actionResponse = TestConnector.action6(testConnector, "Hello", "World");
        message response = {};
        messages:setStringPayload(response, actionResponse);
        reply response;
    }
}