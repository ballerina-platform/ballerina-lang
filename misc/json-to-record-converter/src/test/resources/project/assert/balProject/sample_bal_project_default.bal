type Address record {|
    string city;
    string country;
    int zip?;
    int houseNo?;
    json...;
|};

type Friend_01 record {|
    string firstName;
    string lastName;
    Address address;
    json...;
|};

type NewRecord_01 record {|
    string firstName;
    string lastName;
    Address address;
    Friend_01 friend;
    json...;
|};
