import ballerina/io;

type Person record {
    int a = 0;
    string fname = "John";
    string lname = "";
    Info|error|() info1 = ();
    Info|() info2 = ();
};

type Info record {
    Address|error|() address1 = ();
    Address|() address2 = ();
};

type Address record {
    string street = "";
    string city = "";
    string country = "Sri Lanka";
};

function testNotNilPath () returns any|error {
    Address adrs = {city:"Colombo"};
    Info inf = {address2 : adrs};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    string|error|() x = p?.info2?.address2?.city;
    return x;
}

function testNilInMiddle () returns any|error {
    Info inf = {address2 : ()};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    string|error|() y = p?.info2?.address2?.city;
    return y;
}

function testNilInFirstVar () returns any|error {
    Person|() p = ();
    string|error|() y = p?.info2?.address2?.city;
    return y;
}

function testSafeNavigatingNilJSON_1 () returns any|error {
    json j = {};
    return j.foo;
}

function testSafeNavigatingNilJSON_3 () returns any|error {
    json j = {};
    return j.foo.bar;
}

function testSafeNavigatingNilMap () returns any {
    map<any> m = {};
    return m["foo"];
}

function getNullablePerson() returns Person|() {
    Info inf = {address2 : ()};
    Person prsn = {info2 : inf};
    Person|() p = prsn;
    return p;
}

type Employee object {
  public function getName() returns string {
     return ("John");
  }
};

function testNonExistingMapKeyWithIndexAccess() returns string? {
    map<string> m = {};
    return m["a"];
}

type A record {
    map<any>? foo = ();
};

type Student object {
    public string name = "";
    public int marks = 60;

    function increaseMarks() {
        self.marks = self.marks + 1;
    }
};

type PersonObject object {
    public int a = 0;
    public string fname = "John";
    public string lname = "";
    public InfoObject|()|error info1 = ();
    public InfoObject|() info2;

    function __init(InfoObject|() info2) {
        self.info2 = info2;
    }
};

type InfoObject object {
    public AddressObject|()|error address1 = ();
    public AddressObject|() address2;

    function __init(AddressObject address2) {
        self.address2 = address2;
    }
};

type AddressObject object {
    public string street;
    public string city = "";
    public string country = "Sri Lanka";

    function __init(string street) {
        self.street = street;
    }
};

function getJsonValue() returns json|error {
    return 10;
}

