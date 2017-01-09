package samples.connectors.test;

import ballerina.lang.system;

connector TestConnector(string param) {

    boolean isAction1Invoked;

    action action1(TestConnector testConnector) {
           isAction1Invoked = true;
    }
    
    action action2(TestConnector testConnector) (boolean) {
        return isAction1Invoked;
    }

    action action3(TestConnector testConnector) (string) {
        return param;
    }
}


function callConnector()(boolean, string) {
    samples.connectors.test:TestConnector testConnector=new samples.connectors.test:TestConnector("MyParameter");
    boolean actionCalled;
    string parameter;
    samples.connectors.test:TestConnector.action1(testConnector);
    actionCalled = samples.connectors.test:TestConnector.action2(testConnector);
    parameter = samples.connectors.test:TestConnector.action3(testConnector);

    return actionCalled,parameter;
}