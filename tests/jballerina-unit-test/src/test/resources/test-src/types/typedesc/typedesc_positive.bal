function testBasicTypes() returns [typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>] {
    typedesc<int> a = int;
    typedesc<string> b = string;
    typedesc<float> c = float;
    typedesc<boolean> d = boolean;
    typedesc<byte> e = byte;
    return [a, b, c, d, e];
}

function testRefTypes() returns [typedesc<any>, typedesc<any>, typedesc<any>, typedesc<any>] {
    typedesc<xml> a = xml;
    typedesc<json> b = json;
    typedesc<map<any>> c = map<any>;
    typedesc<table<Employee>> d = table<Employee>;
    return [a, b, c, d];
}

function testObjectTypes() returns [typedesc<any>, typedesc<any>] {
    typedesc<Person> a = Person;
    typedesc<any> b = object {
        public string name = "";
    };
    return [a,b];
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


function testArrayTypes() returns [typedesc<any>, typedesc<any>] {
    typedesc<int[]> a = int[];
    typedesc<int[][]> b = int[][];
    return [a,b];
}

function testRecordTypes() returns [typedesc<any>, typedesc<any>] {
    typedesc<RecordA> a = RecordA;
    typedesc<any> b = record {string c; int d;};
    return [a,b];
}

type RecordA record {
    string a;
    int b;
};

function testTupleUnionTypes() returns [typedesc<any>, typedesc<any>] {
    typedesc<any> a = [string, Person];
    typedesc<int|string> b = int|string;
    return [a,b];
}

function testTuplesWithExpressions() returns typedesc<any> {
    int[] fib = [1, 1, 2, 3, 5, 8];
    typedesc<any> desc = ["foo", 25, ["foo", "bar", "john"], utilFunc(), fib[4]];
    return desc;
}

function testAnyToTypedesc() returns typedesc<any>|error {
    any a = int;
    typedesc<int> desc = <typedesc<int>>a;
    return desc;
}

function utilFunc() returns string {
    return "util function";
}


typedesc<any> glbTypeDesc = json;

function testModuleLevelTypeDesc() returns typedesc<any> {
    return glbTypeDesc;
}

function testMethodLevelTypeDesc() returns typedesc<any> {
    typedesc<any> methodLocalTypeDesc = json;
    return methodLocalTypeDesc;
}
