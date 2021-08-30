public type BaseConnector object {
    public function pushText1(string text) returns error?;
    public function pushText2(string text) returns error?;
    public function pushText3(string text) returns error?;
};

public class Connector {
    *BaseConnector;
    public function pushText1(string text) returns error? {
        return;
    }
}

public class WSConnector {
    *BaseConnector;
}
