type Person {
    int a;
    string fname = "John";
    string lname;
    Info|error info1;
    Info|() info2;
};

type Info {
    Address|error address1;
    Address|() address2;
};

type Address {
    string street;
    string city;
    string country = "Sri Lanka";
};

function testErrorLiftingAcessWithoutErrorOnLHS () returns any {
    Address adrs = {city:"Colombo"};
    Info inf = {address1 : adrs};
    Person prsn = {info1 : inf};
    Person|error p = prsn;
    string|() x = p!info1!address1!city;
    return x;
}

function testFieldAcessWithoutErrorLifting () returns any {
    error e = {message:"custom error"};
    Info inf = {address1 : e};
    Person prsn = {info1 : inf};
    Person|error p = prsn;
    string|error|() x = p!info1.address1!city;
    return x;
}

function testFunctionInvocReturnType () {
    json j;
    string s = j.toString();
    string[] keys = j.getKeys();
}

function testErrorLiftingOnRHS() {
    Person|error p;
    p!info1!address1!city = "Colombo";
}

function testErrorLiftingOnArray() returns Person|error {
    Person[]|error p;
    return p[0];
}
