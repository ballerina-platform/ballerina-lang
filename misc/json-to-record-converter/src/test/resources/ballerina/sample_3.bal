type Address record {
    string streetAddress;
    string city;
    string state;
};

type NewRecord record {
    string firstName;
    string lastName;
    string gender;
    int age;
    Address address;
    anydata phoneNumber?;
};
