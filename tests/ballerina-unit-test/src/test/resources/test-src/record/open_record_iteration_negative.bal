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
        i++;
    }
}

function testInvalidForeachOpWithOpenRecords() {
    p.foreach((any entry) => {
    });

    p.foreach(((string, string, any) entry) => {
    });
}

function testInvalidMapOpWithOpenRecords() {
    map newp = p.map((any entry) => (string, any) {
        return ("", "");
    });

    newp = p.map(((string, string, any) entry) => (string, any) {
        return ("", "");
    });

    newp = p.map(((string, any) entry) => any {
        return "";
    });

    newp = p.map(((string, any) entry) => (string, any, string) {
        return ("", "", "");
    });

    Person invMap = p.map(((string, any) entry) => (string, any) {
        return ("", "");
    });
}

function testInvalidFilterOpWithOpenRecords() {
    map newp = p.filter(((string,any) entry) => boolean {
        return true;
    });

    newp = p.filter(((string, string, any) entry) => boolean {
        return true;
    });

    newp = p.filter(((string, any) entry) => string {
        return "";
    });

    newp = p.filter(((string, any) entry) => (string, any, string) {
        return ("", "", "");
    });

    Person invFil = p.filter(((string, any) entry) => boolean {
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

    map<int> m = f.map(((string, int) entry) => (string, int) {
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
}

function testInvalidChainedItrOpReturns2() {
    RestrictedGrades f = {maths: 80, physics: 75, chemistry: 65, english: 78};

    int[] ar = f.map((int grade) => int {
        return grade + 10;
    })
    .map((int grade) => string {
        if (grade > 75) {
            return "PASS";
        }
        return "FAIL";
    })
    .filter((string status) => boolean {
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .map((string status) => float {
        if (status == "PASS") {
            return 4.2;
        }
        return 0.0;
    });
}
