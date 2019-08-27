import ballerina/http;

type testObject object {
    int field1 = 12;
    int firld2 = 12;

    public function __init(int field1, int field2) {
        self.field1 = field1;
        self.field2 = field2;
    }
};

function testObjectInit() {
    http:Response res = new http:
}
