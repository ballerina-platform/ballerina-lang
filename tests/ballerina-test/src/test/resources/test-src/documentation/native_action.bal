documentation {Test Connector
P{{url}} url for endpoint
P{{path}} path for endpoint
}
connector TestConnector (string url, string path) {

    documentation {Test Connector action testAction P{{s}} which represent successful or not}
    native action testAction() (boolean s);

}