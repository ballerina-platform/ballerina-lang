function testBasicTypes() returns [typedesc<int>, typedesc<string>, typedesc<float>, typedesc<boolean>, typedesc<byte>] {
    typedesc<int> a = int;
    typedesc<string> b = string;
    typedesc<float> c = float;
    typedesc<boolean> d = boolean;
    typedesc<byte> e = byte;
    return [a, b, c, d, e];
}

function testRefTypes() returns [typedesc<xml>, typedesc<json>, typedesc<map<any>>, typedesc<table<Employee>>] {
    typedesc<xml> a = xml;
    typedesc<json> b =
        json;
    typedesc<map<any>> c = map<any>;
    typedesc<table<Employee>> d =
        table<Employee>;
    return [a, b, c, d];
}

function testObjectTypes() returns [typedesc<Person>, typedesc<object {}>] {
    typedesc<Person> a = Person;
    typedesc<object {}> b = object {
        public string name = "";
    };
    return [a, b];
}

type Person object {
    public string name;

    function __init(string name) {
        self.name = name;
    }

    public function getName() returns string {
        return self.name;
    }
};


type Employee record {
    string name;
};


function testArrayTypes() returns [typedesc<int[]>, typedesc<int[][]>] {
    typedesc<int[]> a = int[];
    typedesc<int[][]> b = int[][];
    return [a, b];
}

function testRecordTypes() returns [typedesc<RecordA>, typedesc<record {}>] {
    typedesc<RecordA> a = RecordA;
    typedesc<record {}> b = record {string c; int d;};
    return [a, b];
}

function testRecordTypes2() returns [typedesc<RecordA>, typedesc<record {}>] {
    typedesc<RecordA> a = RecordA;
    typedesc<record {}> b =
        record {string c; int d;};
    return [a, b];
}

function testRecordTypes3() returns [typedesc<RecordA>, typedesc<record {}>] {
    typedesc<RecordA> a = RecordA;
    typedesc<record {}> b = record {
        string c;
        int d;
    };
    return [a, b];
}

type RecordA record {
    string a;
    int b;
};

function testTupleUnionTypes() returns [typedesc<[string, Person]>, typedesc<int | string>] {
    typedesc<[string, Person]> a = [string, Person];
    typedesc<int | string> b = int | string;
    return [a, b];
}

function testTuplesWithExpressions() returns typedesc<any> {
    int[] fib = [1, 1, 2, 3, 5, 8];
    typedesc<any> desc = ["foo", 25, ["foo", "bar", "john"], utilFunc(), fib[4]];
    return desc;
}

function testAnyToTypedesc() returns typedesc<any> | error {
    any a = int;
    typedesc<any> desc = <typedesc<any>>a;
    return desc;
}

function utilFunc() returns string {
    return "util function";
}
