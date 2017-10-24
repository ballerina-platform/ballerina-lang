connector TestConnector(string param1) {

    boolean b1;

    action foo() (boolean){
        return b1;
    }
}


connector TestConnector(string param1, string param2) {

    boolean b2;

    action bar() (boolean){
        return b2;
    }
}