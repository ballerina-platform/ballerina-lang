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

type RestrictedFoo record {
    string a;
    string b;
    string c;
    string d;
    string e;
    string...
};

type Grades record {
    int maths;
    int physics;
    int chemistry;
};

type RestrictedGrades record {
    int maths;
    int physics;
    int chemistry;
    int...
};

type RestrictedBar record {
    float x;
    float y;
    float z;
    float...
};

function testForeachWithOpenRecords() returns (string[], any[]) {
    Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" } };
    string[] fields = [];
    any[] values = [];

    int i = 0;
    foreach f, v in p {
        fields[i] = f;
        values[i] = v;
        i += 1;
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
        i += 1;
    }

    return (fields, values);
}

function testForeachWithOpenRecords3() returns any[] {
    Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" } };
    any[] values = [];

    int i = 0;
    foreach v in p {
        values[i] = v;
        i += 1;
    }

    return values;
}

function testForeachOpWithOpenRecords() returns map {
    Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }, height: 5.9 };
    map rec;

    p.foreach(function ((string, any) entry) {
            var (field, value) = entry;
            rec[field] = value;
        });

    return rec;
}

function testMapOpWithOpenRecords() returns map {
    Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }, profession: "Software Engineer" };

    map newp =  p.map(function ((string, any) entry) returns (string, any) {
           var (field, value) = entry;
            match value {
                string str => value = str.toLower();
                any => {}
            }
            return (field, value);
        });
        
    return newp;
}

function testFilterOpWithOpenRecords() returns map {
    Foo f = {a: "A", b: "B", c: "C", d: "D", e: "E", f: "F"};

    map newf = f.filter(function ((string, any) entry) returns boolean {
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

function testChainedOpsWithOpenRecords() returns map {
    Foo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE", f: "FF"};

    map newf = f.map(function ((string, any) entry) returns (string, any) {
                    var (field, value) = entry;
                    match value {
                        string str => value = str.toLower();
                        any => {}
                    }
                    return (field, value);
                })
                .filter(function ((string, any) entry) returns boolean {
                    var (field, value) = entry;
                    if (value != "aa" && value != "ee") {
                        return true;
                    }
                    return false;
                });

    return newf;
}

function testMapWithAllStringOpenRecord() returns (map<string>, string[]) {
    RestrictedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE", f: "FF"};

    map<string> modFooMap = f.map(function ((string, string) entry) returns (string, string) {
        var (k, v) = entry;
        return (k, v.toLower());
    });

    string[] modFooAr = f.map(function (string val) returns string { return val.toLower(); });

    return (modFooMap, modFooAr);
}

function testMapWithAllIntOpenRecord(int m, int p, int c, int e) returns (map<int>, int[]) {
    RestrictedGrades grades = {maths: m, physics: p, chemistry: c, english: e};

    map<int> adjGrades = grades.map(function ((string, int) entry) returns (string, int) {
        var (subj, grade) = entry;
        return (subj, grade + 10);
    });

    int[] adjGradesAr = grades.map(function (int grade) returns int { return grade + 10; });

    return (adjGrades, adjGradesAr);
}

function testMapWithAllFloatOpenRecord(float a, float b, float c) returns (map<float>, float[]) {
    RestrictedBar bar = {x: a, y: b, z: c, p: 9.9};

    map<float> modBar = bar.map(function ((string, float) entry) returns (string, float) {
        var (k, val) = entry;
        return (k, val + 10);
    });

    float[] modBarAr = bar.map(function (float val) returns float { return val + 10; });

    return (modBar, modBarAr);
}

function testFilterWithAllStringOpenRecord() returns (map<string>, string[]) {
    RestrictedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE", f: "FF"};

    map<string> modFooMap = f.filter(function ((string, string) entry) returns boolean {
        var (k, v) = entry;
        if (v == "AA" || v == "EE" || v == "FF") {
            return true;
        }
        return false;
    });

    string[] modFooAr = f.filter(function (string val) returns boolean {
         if (val == "AA" || val == "EE" || val == "FF") {
             return true;
         }
         return false;
    });

    return (modFooMap, modFooAr);
}

function testFilterWithAllIntOpenRecord() returns (map<int>, int[]) {
    RestrictedGrades grades = {maths: 80, physics: 75, chemistry: 65, english: 78};

    map<int> adjGrades = grades.filter(function ((string, int) entry) returns boolean {
        var (subj, grade) = entry;
        if (grade > 70) {
            return true;
        }
        return false;
    });

    int[] adjGradesAr = grades.filter(function (int grade) returns boolean {
        if (grade > 70) {
            return true;
        }
        return false;
    });

    return (adjGrades, adjGradesAr);
}

function testFilterWithAllFloatOpenRecord(float a, float b, float c) returns (map<float>, float[]) {
    RestrictedBar bar = {x: a, y: b, z: c, p: 9.9};

    map<float> modBar = bar.filter(function ((string, float) entry) returns boolean {
        var (k, val) = entry;
        if (val > 6) {
            return true;
        }
        return false;
    });

    float[] modBarAr = bar.filter(function (float val) returns boolean {
        if (val > 6) {
            return true;
        }
        return false;
    });

    return (modBar, modBarAr);
}

function testTerminalOpsOnAllIntOpenRecord(int m, int p, int c, int e) returns (int, int, int, int, float) {
    RestrictedGrades grades = {maths: m, physics: p, chemistry: c, english: e};

    int count = grades.count();
    int max = grades.max();
    int min = grades.min();
    int sum = grades.sum();
    float avg = grades.average();

    return (count, max, min, sum, avg);
}

function testTerminalOpsOnAllIntOpenRecord2(int m, int p, int e) returns (int, int, int, int, float) {
    RestrictedGrades grades = {maths: m, physics: p, english: e};

    int count = grades.count();
    int max = grades.max();
    int min = grades.min();
    int sum = grades.sum();
    float avg = grades.average();

    return (count, max, min, sum, avg);
}

function testChainedOpsWithOpenRecords2() returns map<float> {
    RestrictedGrades grades = {maths: 80, physics: 75, chemistry: 65, english: 78};

    map<float> m = grades.map(function ((string, int) entry) returns (string, int) {
        var (subj, grade) = entry;
        return (subj, grade + 10);
    })
    .map(function ((string, int) entry) returns (string, string) {
        var (s, g) = entry;
        if (g > 75) {
            return (s, "PASS");
        }
        return (s, "FAIL");
    })
    .filter(function ((string, string) entry) returns boolean {
        var (s, status) = entry;
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .map(function ((string, string) entry) returns (string, float) {
        var (s, status) = entry;
        if (status == "PASS") {
            return (s, 4.2);
        }
        return (s, 0.0);
    });

    return m;
}

function testChainedOpsWithOpenRecords3() returns map<float> {
    Grades grades = {maths: 80, physics: 75, chemistry: 65, english: 78};

    map<float> m = grades.map(function ((string, any) entry) returns (string, int) {
        var (subj, grade) = entry;
        match grade {
            int g => return (subj, g + 10);
            any => {}
        }
        return (subj, -1);
    })
    .map(function ((string, int) entry) returns (string, string) {
        var (s, g) = entry;
        if (g > 75) {
            return (s, "PASS");
        }
        return (s, "FAIL");
    })
    .filter(function ((string, string) entry) returns boolean {
        var (s, status) = entry;
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .map(function ((string, string) entry) returns (string, float) {
        var (s, status) = entry;
        if (status == "PASS") {
            return (s, 4.2);
        }
        return (s, 0.0);
    });

    return m;
}

function testOpChainsWithTerminalOps(int m, int p, int c) returns (int, int, int, int, float) {
    RestrictedGrades f = {maths: m, physics: p, chemistry: c, english: 78};

    int count = f.map(mapTo).filter(filter).count();
    int sum = f.map(mapTo).filter(filter).sum();
    int max = f.map(mapTo).filter(filter).max();
    int min = f.map(mapTo).filter(filter).min();
    float avg = f.map(mapTo).filter(filter).average();

    return (count, sum, max, min, avg);
}

function mapTo((string, int) entry) returns int {
    var (subj, grade) = entry;
    return grade + 10;
}

function filter(int grade) returns boolean {
    if (grade > 75) {
        return true;
    }
    return false;
}

function testMutability() returns RestrictedGrades {
    RestrictedGrades grades = {maths: 80, physics: 75, chemistry: 65, english: 78};

    map<float> m = grades.map(function ((string, int) entry) returns (string, int) {
        var (subj, grade) = entry;
        return (subj, grade + 10);
    })
    .map(function ((string, int) entry) returns (string, string) {
        var (s, g) = entry;
        if (g > 75) {
            return (s, "PASS");
        }
        return (s, "FAIL");
    })
    .filter(function ((string, string) entry) returns boolean {
        var (s, status) = entry;
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .map(function ((string, string) entry) returns (string, float) {
        var (s, status) = entry;
        if (status == "PASS") {
            return (s, 4.2);
        }
        return (s, 0.0);
    });

    return grades;
}
