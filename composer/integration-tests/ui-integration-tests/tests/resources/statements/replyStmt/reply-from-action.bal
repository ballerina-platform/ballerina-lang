connector TestConnector(string param1) {

    boolean b1;

    action testReplyAction(TestConnector testConnector) (boolean){
        reply b1;
    }
}
