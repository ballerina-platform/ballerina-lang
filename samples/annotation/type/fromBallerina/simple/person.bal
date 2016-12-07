package sample.annotation.type.fromBal.simple;

type Person {
    int userID;
    string name;
    string birthday;
    Address address;
    string[] email;
}

type Address {
    string addressLine;
    string city;
    string state;
    string country;
    int zipcode;
}