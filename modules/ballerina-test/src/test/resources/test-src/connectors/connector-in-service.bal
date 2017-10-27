import ballerina.net.http;

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
        s = echoConnector.echoAction(actionParam);
        return s;
    }

    action action6(string echoConnectorParam, string actionParam) (string) {
        EchoConnector localEchoConnector = create EchoConnector(echoConnectorParam);
        string s;

        s =  localEchoConnector.echoAction(actionParam);
        return s;
    }
}

connector EchoConnector(string greeting) {

    action echoAction(string name) (string) {
        return greeting + ", " + name;
    }

}

@http:configuration {
    basePath:"/invoke"
}
service<http> actionInvokeService {

    TestConnector testConnector = create TestConnector(myConst, "MyParam2", 5);

    @http:resourceConfig {
        methods:["GET"],
        path : "/action3"
    }
    resource action3Resource (http:Request req, http:Response res) {

        string actionResponse;
        actionResponse = testConnector.action3();
        res.setStringPayload(actionResponse);
        res.send();
    }

    
    @http:resourceConfig {
        methods:["GET"],
        path : "/action1"
    }
    resource action1Resource (http:Request req, http:Response res) {

        boolean actionResponse;
        actionResponse = testConnector.action1();
        string payload = <string> actionResponse;
        res.setStringPayload(payload);
        res.send();
    }
    
    @http:resourceConfig {
        methods:["GET"],
        path : "/action2"
    }
    resource action2Resource (http:Request req, http:Response res) {

        testConnector.action2();
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/action5"
    }
    resource action5Resource (http:Request req, http:Response res) {

        string actionResponse;
        actionResponse = testConnector.action5(myConst);
        res.setStringPayload(actionResponse);
        res.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path : "/action6"
    }
    resource action6Resource (http:Request req, http:Response res) {

        string actionResponse;
        actionResponse = testConnector.action6("Hello", "World");
        res.setStringPayload(actionResponse);
        res.send();
    }
}