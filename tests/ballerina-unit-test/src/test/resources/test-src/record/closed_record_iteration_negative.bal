type ClosedPerson record {
    string name;
    int age;
    ClosedAddress address;
};

type ClosedAddress record {
    string street;
    string city;
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

    foreach val in p {
        vals[i] = val;
        i++;
    }
}

function testInvalidForeachOpWithClosedRecords() {
    p.foreach((any entry) => {
    });

    p.foreach(((string, string, any) entry) => {
    });
}

function testInvalidMapOpWithClosedRecords() {
    ClosedPerson newp = p.map((any entry) => (string, any) {
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
}

function testInvalidFilterOpWithClosedRecords() {
    ClosedPerson newp = p.filter((any entry) => boolean {
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
}
