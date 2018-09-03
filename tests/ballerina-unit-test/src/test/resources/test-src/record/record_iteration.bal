// OPEN RECORDS

type Person record {
    string name;
    int age;
    Address address;
};

type Address record {
    string street;
    string city;
};

type Foo record {
    string a;
    string b;
    string c;
    string d;
    string e;
};

function testForeachWithOpenRecords() returns (string[], any[]) {
    Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" } };
    string[] fields = [];
    any[] values = [];

    int i = 0;
    foreach f, v in p {
        fields[i] = f;
        values[i] = v;
        i++;
    }

    return (fields, values);
}

function testForeachWithOpenRecords2() returns (string[], any[]) {
    Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }, height: 5.9 };
    string[] fields = [];
    any[] values = [];

    int i = 0;
    foreach f, v in p {
        fields[i] = f;
        values[i] = v;
        i++;
    }

    return (fields, values);
}

function testForeachOpWithOpenRecords() returns map {
    Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }, height: 5.9 };
    map rec;

    p.foreach(((string, any) entry) => {
            var (field, value) = entry;
            rec[field] = value;
        });

    return rec;
}

function testMapOpWithOpenRecords() returns Person {
    Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }, profession: "Software Engineer" };

    Person newp =  p.map(((string, any) entry) => (string, any) {
           var (field, value) = entry;
            match value {
                string str => value = str.toLower();
                any => {}
            }
            return (field, value);
        });
        
    return newp;
}

function testFilterOpWithOpenRecords() returns Foo {
    Foo f = {a: "A", b: "B", c: "C", d: "D", e: "E", f: "F"};

    Foo newf = f.filter(((string, any) entry) => boolean {
        var (field, value) = entry;
        if (value != "A" && value != "E") {
            return true;
        }
        return false;
    });

    return newf;
}

function testCountOpWithOpenRecords() returns int {
    Foo f = {a: "A", b: "B", c: "C", d: "D", e: "E", f: "F"};
    return f.count();
}

function testChainedOpsWithOpenRecords() returns Foo {
    Foo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE", f: "FF"};

    Foo newf = f.map(((string, any) entry) => (string, any) {
                    var (field, value) = entry;
                    match value {
                        string str => value = str.toLower();
                        any => {}
                    }
                    return (field, value);
                })
                .filter(((string, any) entry) => boolean {
                    var (field, value) = entry;
                    if (value != "aa" && value != "ee") {
                        return true;
                    }
                    return false;
                });

    return newf;
}


// CLOSED RECORDS

type ClosedPerson record {
    string name;
    int age;
    Address address;
    !...
};

type ClosedAddress record {
    string street;
    string city;
    !...
};

type ClosedFoo record {
    string a;
    string b;
    string c;
    string d;
    string e;
    !...
};

function testForeachWithClosedRecords() returns (string[], any[]) {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" } };
    string[] fields = [];
    any[] values = [];

    int i = 0;
    foreach f, v in p {
        fields[i] = f;
        values[i] = v;
        i++;
    }

    return (fields, values);
}

function testForeachOpWithClosedRecords() returns map {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }};
    map rec;

    p.foreach(((string, any) entry) => {
            var (field, value) = entry;
            rec[field] = value;
        });

    return rec;
}

function testMapOpWithClosedRecords() returns ClosedPerson {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }};

    ClosedPerson newp =  p.map(((string, any) entry) => (string, any) {
           var (field, value) = entry;
            match value {
                string str => value = str.toLower();
                any => {}
            }
            return (field, value);
        });
        
    return newp;
}

function testFilterOpWithClosedRecords() returns Foo {
    ClosedFoo f = {a: "A", b: "B", c: "C", d: "D", e: "E"};

    ClosedFoo newf = f.filter(((string, any) entry) => boolean {
        var (field, value) = entry;
        if (value == "A" || value == "E") {
            return true;
        }
        return false;
    });

    return newf;
}

function testCountOpWithClosedRecords() returns int {
    ClosedFoo f = {a: "A", b: "B", c: "C", d: "D", e: "E"};
    return f.count();
}

function testChainedOpsWithClosedRecords() returns Foo {
    Foo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE"};

    Foo newf = f.map(((string, any) entry) => (string, any) {
                    var (field, value) = entry;
                    match value {
                        string str => value = str.toLower();
                        any => {}
                    }
                    return (field, value);
                })
                .filter(((string, any) entry) => boolean {
                    var (field, value) = entry;
                    if (value != "aa" && value != "ee") {
                        return true;
                    }
                    return false;
                });

    return newf;
}
