type firstRec record {
    int id = 1;
    string name = "default";
};

type secondRec record {
    int f1 = 1;
    string f2 = "default";
};

type thirdRec record {
    int f1;
    string f2;
    int f4?;
};

function waitForOneTest() {
    future< int > f1 = start getId();
    future< string > f2 = start getName();
    future< boolean > f3 = start getStatus();
    future< int > f4 = start getId();

    // Valid
    any s1 = wait f1;
    int s2 = wait f1;

    string s3 = wait f1;
    wait f2;
}

function waitForAnyTest() {
    future< int > f1 = start getId();
    future< string > f2 = start getName();
    future< boolean > f3 = start getStatus();
    future< int > f4 = start getId();

    // Valid
    int|string|boolean validRes1 = wait f1 |f2|f3;
    var validRes2 = wait f1 |f2|f3;
    int|string validRes3 = wait getStdId() |f2;

    future< int|string > f5 = start getId();
    future< int|float > f6 = start getId();
    int|string|float validRes4 = wait f5 |f6;

    // Invalid scenarios
    int result1 = wait f1|f4 |f2;
    int|boolean result2 = wait f1 |f2|f3;
    map<int|boolean> result3 = wait f1 |f2|f3;
    future< int > result4 = f1|f4;
    int|string result5 = wait ((f1|f2) ? f2 : f1) |f4|f2;// f1 and f2 cannot be future types
    future< int|string> result6 = wait f1|f2;

}

function waitForAllTest() {
    future<int> f1 = start getId();
    future<string> f2 = start getName();
    future<boolean> f3 = start getStatus();
    future<int> f4 = start getId();

    // Valid
    map<int|string> m1 = wait {f1, f2};
    map<int|string> m2 = wait {f1, name: f2};
    var v = wait {f1, f2, f3};
    record { int f1; string f2;} rec1 = wait {f1, f2};
    record {int id; string name;} rec2 = wait {id: getStdId(), name: f2};
    record { int idField; string stringField;} rec3 = wait {idField: f1, stringField: f2};
    firstRec rec4 = wait {id: f1, name: f2};
    secondRec rec5 = wait {f1, f2};
    secondRec rec6 = wait {f1: f1, f2};
    record { int f1; string f3?;} rec7 = wait {f1};
    thirdRec rec8 = wait {f1: f1, f2};

    // Invalid scenarios
    map<int> result7 = wait {f1, f2};
    map<boolean|string> result8 = wait {f1, f4};
    record { int f1; int f2;} result9 = wait {f1, f2};
    record { int f1; string f2;} result10 = wait {f1, f2, f4};
    record { int f1; string f3;} result11 = wait {f1, f2};
    thirdRec result12 = wait {f1: f1};
}

function print(string str) {
    string result = str.toUpper();
}

function getId() returns int {
    return 10;
}

function getName() returns string {
    return "Natasha";
}

function getStatus() returns boolean {
    return true;
}

function getStdId() returns future<int> {
    future <int> id = start getId();
    return id;
}
