package samples.connectors.test;

import ballerina.lang.message;
import ballerina.lang.string;
import ballerina.lang.system;

const string myConst = "MyParam1";

connector TestConnector(string param1, string param2, int param3) {

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
}