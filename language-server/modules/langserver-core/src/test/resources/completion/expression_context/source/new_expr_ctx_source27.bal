type Config record {
    int count = 10;
    string name = "";
};

public client class MyClient {
    function init(string url, *Config config) {

    }
}

MyClient myClient = new("http://example.com", );
