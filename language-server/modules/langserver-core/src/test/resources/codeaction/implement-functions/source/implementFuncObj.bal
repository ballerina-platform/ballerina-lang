public type BaseConnector object {
    public function pushText1(string text) returns error?;
    public function pushText2(string text) returns error?;
    public function pushText3(string text);
};

public class Connector {
    *BaseConnector;
    public function pushText1(string text) returns error? {

    }
}

public class WSConnector {
    *BaseConnector;
}
