type CountryCode "LK"|"USA"|"UK";

type Person record {
    string fname = "default";
    string lname?;
    int age? = 999;
    Address adrs;
};

type Address record {
    string street;
    string city;
    CountryCode country;
};

function testNonDefReqField() returns Person {
    Person p = {adrs:{country: "LK"}};
    return p;
}

function testNonDefReqField2() returns Person {
    Person p = {fname: "John", lname: "Doe", adrs:{country: "LK"}};
    return p;
}

// When adrs is a defaultable field.
type Person2 record {
    string fname = "default";
    string lname?;
    int age? = 999;
    Address2 adrs;
};

type Address2 record {
    string street;
    string city;
    CountryCode country = "LK";
};

function testDefaultableReqField() returns Person2 {
    Person2 p = {};
    return p;
}

// When adrs is an optional field.
type Person3 record {
    string fname = "default";
    string lname?;
    int age? = 999;
    Address3 adrs?;
};

type Address3 record {
    string street;
    string city;
    CountryCode country;
};

function testOptionalNonDefField() returns Person3 {
    Person3 p = {};
    return p;
}

// When adrs is an optional defaultable field.
type Person4 record {
    string fname = "default";
    string lname?;
    int age? = 999;
    Address4 adrs?;
};

type Address4 record {
    string street;
    string city;
    CountryCode country = "LK";
};

function testOptionalDefaultableField() returns Person4 {
    Person4 p = {};
    return p;
}
