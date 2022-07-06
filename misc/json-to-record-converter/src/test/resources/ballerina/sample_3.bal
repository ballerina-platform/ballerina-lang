type Address record {
    (int|string) streetAddress;
    string city;
    string state;
};

type Friend record {
    string firstName;
    string lastName;
    Address address;
};

type NewRecord record {
    string firstName;
    string lastName;
    string gender;
    int age;
    Address address;
    anydata phoneNumber?;
    Friend friend;
};
