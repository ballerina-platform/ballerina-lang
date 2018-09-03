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

    foreach val in p {
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
    Person newp = p.map((any entry) => (string, any) {
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

function testInvalidFilterOpWithOpenRecords() {
    Person newp = p.filter((any entry) => boolean {
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
