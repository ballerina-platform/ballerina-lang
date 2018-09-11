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

type Address record {
    string street;
    string city;
};

type ClosedFoo record {
    string a;
    string b;
    string c;
    string d;
    string e;
    !...
};

type ClosedGrades record {
    int maths;
    int physics;
    int chemistry;
    !...
};

type ClosedBar record {
    float x;
    float y;
    float z;
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

function testForeachWithOpenRecords2() returns any[] {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" } };
    any[] values = [];

    int i = 0;
    foreach v in p {
        values[i] = v;
        i++;
    }

    return values;
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

function testMapOpWithClosedRecords() returns map {
    ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }};

    map newp =  p.map(((string, any) entry) => (string, any) {
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

    map<string> newf = f.filter(((string, string) entry) => boolean {
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

    map<string> newf = f.map(((string, string) entry) => (string, string) {
                    var (field, value) = entry;
                    return (field, value.toLower());
                })
                .filter(((string, string) entry) => boolean {
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

    map<string> modFooMap = f.map(((string, string) entry) => (string, string) {
        var (k, v) = entry;
        return (k, v.toLower());
    });

    string[] modFooAr = f.map((string val) => string { return val.toLower(); });

    return (modFooMap, modFooAr);
}

function testMapWithAllIntClosedRecord(int m, int p, int c) returns (map<int>, int[]) {
    ClosedGrades grades = {maths: m, physics: p, chemistry: c};

    map<int> adjGrades = grades.map(((string, int) entry) => (string, int) {
        var (subj, grade) = entry;
        return (subj, grade + 10);
    });

    int[] adjGradesAr = grades.map((int grade) => int { return grade + 10; });

    return (adjGrades, adjGradesAr);
}

function testMapWithAllFloatClosedRecord(float a, float b, float c) returns (map<float>, float[]) {
    ClosedBar bar = {x: a, y: b, z: c};

    map<float> modBar = bar.map(((string, float) entry) => (string, float) {
        var (k, val) = entry;
        return (k, val + 10);
    });

    float[] modBarAr = bar.map((float val) => float { return val + 10; });

    return (modBar, modBarAr);
}

function testFilterWithAllStringClosedRecord() returns (map<string>, string[]) {
    ClosedFoo f = {a: "AA", b: "BB", c: "CC", d: "DD", e: "EE"};

    map<string> modFooMap = f.filter(((string, string) entry) => boolean {
        var (k, v) = entry;
        if (v == "AA" || v == "EE") {
            return true;
        }
        return false;
    });

    string[] modFooAr = f.filter((string val) => boolean {
         if (val == "AA" || val == "EE") {
             return true;
         }
         return false;
    });

    return (modFooMap, modFooAr);
}

function testFilterWithAllIntClosedRecord() returns (map<int>, int[]) {
    ClosedGrades grades = {maths: 80, physics: 75, chemistry: 65};

    map<int> adjGrades = grades.filter(((string, int) entry) => boolean {
        var (subj, grade) = entry;
        if (grade > 70) {
            return true;
        }
        return false;
    });

    int[] adjGradesAr = grades.filter((int grade) => boolean {
        if (grade > 70) {
            return true;
        }
        return false;
    });

    return (adjGrades, adjGradesAr);
}

function testFilterWithAllFloatClosedRecord(float a, float b, float c) returns (map<float>, float[]) {
    ClosedBar bar = {x: a, y: b, z: c};

    map<float> modBar = bar.filter(((string, float) entry) => boolean {
        var (k, val) = entry;
        if (val > 6) {
            return true;
        }
        return false;
    });

    float[] modBarAr = bar.filter((float val) => boolean { 
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

    map<float> m = f.map(((string, int) entry) => (string, int) {
        var (subj, grade) = entry;
        return (subj, grade + 10);
    })
    .map(((string, int) entry) => (string, string) {
        var (s, g) = entry;
        if (g > 75) {
            return (s, "PASS");
        }
        return (s, "FAIL");
    })
    .filter(((string, string) entry) => boolean {
        var (s, status) = entry;
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .map(((string, string) entry) => (string, float) {
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

    map<float> m = grades.map(((string, int) entry) => (string, int) {
        var (subj, grade) = entry;
        return (subj, grade + 10);
    })
    .map(((string, int) entry) => (string, string) {
        var (s, g) = entry;
        if (g > 75) {
            return (s, "PASS");
        }
        return (s, "FAIL");
    })
    .filter(((string, string) entry) => boolean {
        var (s, status) = entry;
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .map(((string, string) entry) => (string, float) {
        var (s, status) = entry;
        if (status == "PASS") {
            return (s, 4.2);
        }
        return (s, 0.0);
    });

    return grades;
}
