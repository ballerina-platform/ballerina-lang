type Person record {
    string name;
    int age;
    Address address;
};

type Address record {
    string street;
    string city;
};

Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }, profession: "Software Engineer" };

function testSumOpWithOpenRecords() {
    var sum = p.sum();
}

function testAverageOpWithOpenRecords() {
    var avg = p.average();
}

function testMaxOpWithOpenRecords() {
    var max = p.max();
}

function testMinOpWithOpenRecords() {
    var min = p.min();
}

function testInvalidArgForForeachWithOpenRecords() {
    any[] vals = [];
    int i = 0;

    foreach k, val, e in p {
        vals[i] = val;
        i += 1;
    }
}

function testInvalidForeachOpWithOpenRecords() {
    p.foreach(function (any entry) {
    });

    p.foreach(function ((string, string, any) entry) {
    });
}

function testInvalidMapOpWithOpenRecords() {
    map newp = p.map(function (any entry) returns (string, any) {
        return ("", "");
    });

    newp = p.map(function ((string, string, any) entry) returns (string, any) {
        return ("", "");
    });

    newp = p.map(function ((string, any) entry) returns any {
        return "";
    });

    newp = p.map(function ((string, any) entry) returns (string, any, string) {
        return ("", "", "");
    });

    Person invMap = p.map(function ((string, any) entry) returns (string, any) {
        return ("", "");
    });
}

function testInvalidFilterOpWithOpenRecords() {
    map newp = p.filter(function ((string,any) entry) returns boolean {
        return true;
    });

    newp = p.filter(function ((string, string, any) entry) returns boolean {
        return true;
    });

    newp = p.filter(function ((string, any) entry) returns string {
        return "";
    });

    newp = p.filter(function ((string, any) entry) returns (string, any, string) {
        return ("", "", "");
    });

    Person invFil = p.filter(function ((string, any) entry) returns boolean {
        return false;
    });
}


type RestrictedGrades record {
    int maths;
    int physics;
    int chemistry;
    int...
};


function testInvalidChainedItrOpReturns() {
    RestrictedGrades f = {maths: 80, physics: 75, chemistry: 65, english: 78};

    map<int> m = f.map(function ((string, int) entry) returns (string, int) {
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
}

function testInvalidChainedItrOpReturns2() {
    RestrictedGrades f = {maths: 80, physics: 75, chemistry: 65, english: 78};

    int[] ar = f.map(function (int grade) returns int {
        return grade + 10;
    })
    .map(function (int grade) returns string {
        if (grade > 75) {
            return "PASS";
        }
        return "FAIL";
    })
    .filter(function (string status) returns boolean {
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .map(function (string status) returns float {
        if (status == "PASS") {
            return 4.2;
        }
        return 0.0;
    });
}
