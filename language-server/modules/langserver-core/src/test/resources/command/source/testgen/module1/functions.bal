import ballerina/http;
import ballerina/task;

public function returnInt(int n1, int n2, int n3) returns int {
    return 1;
}

public function returnIntArray(int n1, int n2, int n3) returns int[] {
    return [20];
}

public function returnTuple(int n1, int n2, int n3) returns (int, float) {
    return (300, 4.0);
}

public function returnUnion(int n1, int n2, int n3) returns (int|string) {
    return 5000;
}

public function returnMap(int n1, int n2, int n3) returns map<int> {
    map<int> m = { key: n1 };
    return m;
}

public function returnConstrainedMap(int n1, int n2, int n3) returns map<string> {
    map<string> m = { key: "value" };
    return m;
}

public function returnBoolean(int n1, int n2, int n3) returns boolean {
    boolean b = true;
    return b;
}

public function returnXml(int n1, int n2, int n3) returns xml {
    xml m = xml` `;
    return m;
}

public function returnJson(int n1, int n2, int n3) returns json {
    json j = {};
    return j;
}

function functionPointerAsParam(int a, function (int x, int y) returns (int) func) returns (int) {
    int x = a + func.call(6, 70);
    return x;
}

public function returnOptionalError(int n1, int n2, int n3) returns error? {
    return ();
}

public function returnOptionalAny(int n1, int n2, int n3) returns any? {
    return 1;
}

public function complexInput(task:Timer timer) returns error? {
    return ();
}

function complexReturnType(string url) returns http:Client {
    http:Client myclient = new("https://postman-echo.com/get?foo1=bar1&foo2=bar2");
    return myclient;
}
