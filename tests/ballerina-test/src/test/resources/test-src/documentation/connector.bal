documentation {
Test Connector
- #url url for endpoint}
connector TestConnector (string url) {

    documentation {Test Connector action testAction
    - #s which represent successful or not}
    action testAction() (boolean s) {
       boolean value;
       return value;
    }

    documentation {Test Connector action testSend
    - #ep which represent successful or not
    - #s which represent successful or not}
    action testSend(string ep) (boolean s) {
        boolean value;
        return value;
    }
}
