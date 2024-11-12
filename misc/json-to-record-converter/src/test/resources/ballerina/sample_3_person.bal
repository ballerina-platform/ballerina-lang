type Address record {|
    (int|string) streetAddress;
    string city;
    string state;
    json...;
|};

type Friend record {|
    string firstName;
    string lastName;
    Address address;
    json...;
|};

type Person record {|
    string firstName;
    string lastName;
    string gender;
    int age;
    Address address;
    json phoneNumber;
    Friend friend;
    json...;
|};
