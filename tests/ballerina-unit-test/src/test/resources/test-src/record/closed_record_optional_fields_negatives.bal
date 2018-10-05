type CountryCode "LK"|"USA"|"UK";

type Person record {
    string fname = "default";
    string lname?;
    int age? = 999;
    Address adrs;
    !...
};

type Address record {
    string street;
    string city;
    CountryCode country;
    !...
};

function testNonDefReqField() {
    Person p1 = {};
    Person p2;
}

// When adrs is an optional field
type Person3 record {
    string fname = "default";
    string lname?;
    int age? = 999;
    Address3 adrs?;
    !...
};

type Address3 record {
    string street;
    string city;
    CountryCode country;
    !...
};

function testOptionalNonDefField() {
    Person3 p = {};
    Address3 a = p.adrs;
}
