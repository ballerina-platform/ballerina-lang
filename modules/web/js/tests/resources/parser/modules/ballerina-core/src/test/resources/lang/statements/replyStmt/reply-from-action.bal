connector TestConnector(string param1) {

    boolean b1;

    action testReplyAction() (boolean){
        reply b1;
    }
}
