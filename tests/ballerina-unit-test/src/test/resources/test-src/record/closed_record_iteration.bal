type ClosedPerson record {
    string name = "";
    int age = 0;
    Address address = {};
    !...
};

type ClosedAddress record {
    string street = "";
    string city = "";
    !...
};

type Address record {
    string street = "";
    string city = "";
};

type ClosedFoo record {
    string a = "";
    string b = "";
    string c = "";
    string d = "";
    string e = "";
    !...
};

type ClosedGrades record {
    int maths = 0;
    int physics = 0;
    int chemistry = 0;
    !...
};

type ClosedBar record {
    float x = 0.0;
    float y = 0.0;
    float z = 0.0;
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
        i += 1;
    }

    return (fields, values);
}

function testForeachWithOpenRecords2() returns any[] {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" } };
    any[] values = [];

    int i = 0;
    foreach v in p {
        values[i] = v;
        i += 1;
    }

    return values;
}

function testForeachOpWithClosedRecords() returns map {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }};
    map rec = {};

    p.foreach(function ((string, any) entry) {
            var (field, value) = entry;
            rec[field] = value;
        });

    return rec;
}

function testMapOpWithClosedRecords() returns map {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }};

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

function testFilterOpWithClosedRecords() returns map<string> {
    ClosedFoo f = {a: "A", b: "B", c: "C", d: "D", e: "E"};

    map<string> newf = f.filter(function ((string, string) entry) returns boolean {
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

function testChainedOpsWithClosedRecords() returns map<string> {
    ClosedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE"};

    map<string> newf = f.map(function ((string, string) entry) returns (string, string) {
                    var (field, value) = entry;
                    return (field, value.toLower());
                })
                .filter(function ((string, string) entry) returns boolean {
                    var (field, value) = entry;
                    if (value != "aa" && value != "ee") {
                        return true;
                    }
                    return false;
                });

    return newf;
}

function testMapWithAllStringClosedRecord() returns (map<string>, string[]) {
    ClosedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE"};

    map<string> modFooMap = f.map(function ((string, string) entry) returns (string, string) {
        var (k, v) = entry;
        return (k, v.toLower());
    });

    string[] modFooAr = f.map(function (string val) returns string { return val.toLower(); });

    return (modFooMap, modFooAr);
}

function testMapWithAllIntClosedRecord(int m, int p, int c) returns (map<int>, int[]) {
    ClosedGrades grades = {maths: m, physics: p, chemistry: c};

    map<int> adjGrades = grades.map(function ((string, int) entry) returns (string, int) {
        var (subj, grade) = entry;
        return (subj, grade + 10);
    });

    int[] adjGradesAr = grades.map(function (int grade) returns int { return grade + 10; });

    return (adjGrades, adjGradesAr);
}

function testMapWithAllFloatClosedRecord(float a, float b, float c) returns (map<float>, float[]) {
    ClosedBar bar = {x: a, y: b, z: c};

    map<float> modBar = bar.map(function ((string, float) entry) returns (string, float) {
        var (k, val) = entry;
        return (k, val + 10);
    });

    float[] modBarAr = bar.map(function (float val) returns float { return val + 10; });

    return (modBar, modBarAr);
}

function testFilterWithAllStringClosedRecord() returns (map<string>, string[]) {
    ClosedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE"};

    map<string> modFooMap = f.filter(function ((string, string) entry) returns boolean {
        var (k, v) = entry;
        if (v == "AA" || v == "EE") {
            return true;
        }
        return false;
    });

    string[] modFooAr = f.filter(function (string val) returns boolean {
         if (val == "AA" || val == "EE") {
             return true;
         }
         return false;
    });

    return (modFooMap, modFooAr);
}

function testFilterWithAllIntClosedRecord() returns (map<int>, int[]) {
    ClosedGrades grades = {maths: 80, physics: 75, chemistry: 65};

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

function testFilterWithAllFloatClosedRecord(float a, float b, float c) returns (map<float>, float[]) {
    ClosedBar bar = {x: a, y: b, z: c};

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

function testTerminalOpsOnAllIntClosedRecord(int m, int p, int c) returns (int, int, int, int, float) {
    ClosedGrades grades = {maths: m, physics: p, chemistry: c};

    int count = grades.count();
    int max = grades.max();
    int min = grades.min();
    int sum = grades.sum();
    float avg = grades.average();

    return (count, max, min, sum, avg);
}

function testTerminalOpsOnAllIntClosedRecord2(int m, int p) returns (int, int, int, int, float) {
    ClosedGrades grades = {maths: m, physics: p};

    int count = grades.count();
    int max = grades.max();
    int min = grades.min();
    int sum = grades.sum();
    float avg = grades.average();

    return (count, max, min, sum, avg);
}

function testChainedOpsWithClosedRecords2() returns map<float> {
    ClosedGrades f = {maths: 80, physics: 75, chemistry: 65};

    map<float> m = f.map(function ((string, int) entry) returns (string, int) {
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

function testOpChainsWithTerminalOps(int m, int p, int c) returns (int, int, int, int, float) {
    ClosedGrades f = {maths: m, physics: p, chemistry: c};

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

function testMutability() returns ClosedGrades {
    ClosedGrades grades = {maths: 80, physics: 75, chemistry: 65};

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
