type Config record {|
    int count = 10;
    string name = "mock";
|};

client class MyClient {
    function init(string url, *Config config) {

    }
}

MyClient cli = new ("", config = {count: 0,});
