function testBasicTypes() returns (typedesc, typedesc, typedesc, typedesc, typedesc) {
    typedesc a = int;
    typedesc b = string;
    typedesc c = float;
    typedesc d = boolean;
    typedesc e = blob;
    return (a, b, c, d, e);
}

function testRefTypes() returns (typedesc, typedesc, typedesc, typedesc) {
    typedesc a = xml;
    typedesc b = json;
    typedesc c = map;
    typedesc d = table<Person>;
    return (a, b, c, d);
}

function testObjectTypes() returns (typedesc, typedesc) {
    typedesc a = Person;
    typedesc b = object {
        public {
            string name;
        }
    };
    return (a,b);
}

type Person object {
    public {
        string name;
    }
    new (name){}
    public function getName() returns string {
        return name;
    }
};

function testArrayTypes() returns (typedesc, typedesc) {
    typedesc a = int[];
    typedesc b = int[][];
    return (a,b);
}

function testRecordTypes() returns (typedesc, typedesc) {
    typedesc a = RecordA;
    typedesc b = {string c, int d,};
    return (a,b);
}

type RecordA {
    string a;
    int b;
};

function testTupleUnionTypes() returns (typedesc, typedesc) {
    typedesc a = (string, Person);
    typedesc b = int|string;
    return (a,b);
}
