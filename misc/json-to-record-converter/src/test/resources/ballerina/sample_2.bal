type Address record {
    string city;
    string country;
    int zip?;
    int houseNo?;
};

type Friend record {
    string firstName;
    string lastName;
    Address address;
};

type NewRecord record {
    string firstName;
    string lastName;
    Address address;
    Friend friend;
};
