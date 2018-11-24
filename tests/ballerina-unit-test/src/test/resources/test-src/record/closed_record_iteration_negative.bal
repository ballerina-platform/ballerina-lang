type ClosedPerson record {
    string name = "";
    int age = 0;
    ClosedAddress address = {};
    !...
};

type ClosedAddress record {
    string street = "";
    string city = "";
    !...
};

type ClosedGrades record {
    int maths = 0;
    int physics = 0;
    int chemistry = 0;
    !...
};

ClosedPerson p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }};

function testSumOpWithClosedRecords() {
    var sum = p.sum();
}

function testAverageOpWithClosedRecords() {
    var avg = p.average();
}

function testMaxOpWithClosedRecords() {
    var max = p.max();
}

function testMinOpWithClosedRecords() {
    var min = p.min();
}

function testInvalidArgForForeachWithClosedRecords() {
    any[] vals = [];
    int i = 0;

    foreach k, val, e in p {
        vals[i] = val;
        i += 1;
    }
}

function testInvalidForeachOpWithClosedRecords() {
    p.foreach(function (any entry) {
    });

    p.foreach(function ((string, string, any) entry) {
    });
}

function testInvalidMapOpWithClosedRecords() {
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

    ClosedPerson invMap = p.map(function ((string, any) entry) returns (string, any) {
        return ("", "");
    });
}

function testInvalidFilterOpWithClosedRecords() {
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

    ClosedPerson invFil = p.filter(function ((string, any) entry) returns boolean {
        return false;
    });
}

function testInvalidChainedItrOpReturns() {
    ClosedGrades f = {maths: 80, physics: 75, chemistry: 65};

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
    ClosedGrades f = {maths: 80, physics: 75, chemistry: 65};

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
