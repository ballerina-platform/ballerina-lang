type DummyType record {
    int id;
};

function testJsonArrayWithUnsupportedtypes() returns (json) {
    table<DummyType> dt = table [];
    json j = ["a", "b", "c", dt];
    return j;
}

function testJsonInitWithUnsupportedtypes() returns (json) {
    table<DummyType> dt = table [];
    json j = { "name": "Supun", "value": dt };
    return j;
}

public function testCloseRecordToMapJsonAssigment() returns [map<json>, map<json>] {
    AnotherPerson ap = {};
    Person p = {};
    PersonWithTypedesc ptdesc = {name: "", t: typeof(33)};
    map<json> pm = p;
    map<json> m = ap;
    map<json> ptdescM = ptdesc;
    return [pm, m];
}

type Person record {|
    string name = "";
    int age = 10;
    Obj obj = new();
    string...;
|};

type AnotherPerson record {|
    string name = "";
    int age = 10 + 20;
    Obj obj = new();
|};

type PersonWithTypedesc record {
    string name = "";
    typedesc<any> t;
};

class Obj {
    int i;

    function init() {
        self.i = 0;
    }
}
