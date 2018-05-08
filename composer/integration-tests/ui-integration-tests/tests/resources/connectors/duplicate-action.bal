
connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action foo(TestConnector testConnector) (boolean){
        return action2Invoked;
    }

    action foo(TestConnector testConnector) {
        action2Invoked = true;
    }
}