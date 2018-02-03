connector TestConnector(string param1, string param2, int param3) {

    boolean action2Invoked;

    action foo() (boolean){
        return action2Invoked;
    }

    action foo() {
        action2Invoked = true;
    }
}