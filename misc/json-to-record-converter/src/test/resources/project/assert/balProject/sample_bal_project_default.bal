type Address record {
    string city;
    string country;
    int zip?;
    int houseNo?;
};

type Friend_01 record {
    string firstName;
    string lastName;
    Address address;
};

type NewRecord_01 record {
    string firstName;
    string lastName;
    Address address;
    Friend_01 friend;
};
