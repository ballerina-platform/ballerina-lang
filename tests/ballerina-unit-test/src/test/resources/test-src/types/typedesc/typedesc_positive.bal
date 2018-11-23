function testBasicTypes() returns (typedesc, typedesc, typedesc, typedesc, typedesc) {
    typedesc a = int;
    typedesc b = string;
    typedesc c = float;
    typedesc d = boolean;
    typedesc e = byte;
    return (a, b, c, d, e);
}

function testRefTypes() returns (typedesc, typedesc, typedesc, typedesc) {
    typedesc a = xml;
    typedesc b = json;
    typedesc c = map;
    typedesc d = table<Employee>;
    return (a, b, c, d);
}

function testObjectTypes() returns (typedesc, typedesc) {
    typedesc a = Person;
    typedesc b = object {
        public string name;
    };
    return (a,b);
}

type Person object {
    public string name;

    new (name){}
    public function getName() returns string {
        return self.name;
    }
};


type Employee record {
    string name;
};


function testArrayTypes() returns (typedesc, typedesc) {
    typedesc a = int[];
    typedesc b = int[][];
    return (a,b);
}

function testRecordTypes() returns (typedesc, typedesc) {
    typedesc a = RecordA;
    typedesc b = record {string c; int d;};
    return (a,b);
}

type RecordA record {
    string a;
    int b;
};

function testTupleUnionTypes() returns (typedesc, typedesc) {
    typedesc a = (string, Person);
    typedesc b = int|string;
    return (a,b);
}

function testTuplesWithExpressions() returns typedesc {
    int[] fib = [1, 1, 2, 3, 5, 8];
    typedesc desc = ("foo", 25, ["foo", "bar", "john"], utilFunc(), fib[4]);
    return desc;
}

function testAnyToTypedesc() returns typedesc|error {
    any a = int;
    typedesc desc = check <typedesc>a;
    return desc;
}

function utilFunc() returns string {
    return "util function";
}
