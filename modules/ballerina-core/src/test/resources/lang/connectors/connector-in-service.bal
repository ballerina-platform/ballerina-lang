package samples.connectors.test;

import ballerina.lang.message;
import ballerina.lang.string;
import ballerina.lang.system;

const string myConst = "MyParam1";

connector TestConnector(string param1, string param2, int param3) {

    test:EchoConnector echoConnector = new test:EchoConnector(param1);

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
        return test:EchoConnector.echoAction(echoConnector, actionParam);
    }

    action action6(TestConnector testConnector, string echoConnectorParam, string actionParam) (string) {
        test:EchoConnector localEchoConnector = new test:EchoConnector(echoConnectorParam);

        system:println("hello");
        system:println(echoConnectorParam);
        return test:EchoConnector.echoAction(localEchoConnector, actionParam);
    }
}

connector EchoConnector(string greeting) {

    action echoAction(EchoConnector echoConnector, string name) (string) {
        return greeting + ", " + name;
    }

}

@BasePath ("/invoke")
service actionInvokeService {

    test:TestConnector testConnector = new test:TestConnector(myConst, "MyParam2", 5);

    @GET
    @Path ("/action3")
    resource action3Resource (message m) {

        string actionResponse;
        message response;

        actionResponse = test:TestConnector.action3(testConnector);
        response = new message;
        message:setStringPayload(response, actionResponse);
        reply response;
    }


    @GET
    @Path ("/action1")
    resource action1Resource (message m) {

        boolean actionResponse;
        message response;

        actionResponse = test:TestConnector.action1(testConnector);
        response = new message;
        message:setStringPayload(response, string:valueOf(actionResponse));
        reply response;
    }


    @GET
    @Path ("/action2")
    resource action2Resource (message m) {

        test:TestConnector.action2(testConnector);
        reply m;
    }

    @GET
    @Path ("/action5")
    resource action5Resource (message m) {

        string actionResponse;
        message response;

        actionResponse = test:TestConnector.action5(testConnector, myConst);
        response = new message;
        message:setStringPayload(response, actionResponse);
        reply response;
    }

    @GET
    @Path ("/action6")
    resource action6Resource (message m) {

        string actionResponse;
        message response;

        actionResponse = test:TestConnector.action6(testConnector, "Hello", "World");
        response = new message;
        message:setStringPayload(response, actionResponse);
        reply response;
    }
}