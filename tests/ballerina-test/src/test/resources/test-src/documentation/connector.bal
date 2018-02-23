documentation {Test Connector
P{{url}} url for endpoint
P{{path}} path for endpoint
}
connector TestConnector (string url, string path) {

    documentation {Test Connector action testAction R{{s}} which represent successful or not}
    action testAction() (boolean s) {
       boolean value;
       return value;
    }

    documentation {Test Connector action testSend P{{ep}} which represent successful or not R{{s}} which represent successful or not}
    action testSend(string ep) (boolean s) {
        boolean value;
        return value;
    }
}
