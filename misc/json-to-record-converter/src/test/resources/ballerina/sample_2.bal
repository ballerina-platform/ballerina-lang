type Address record {|
    string city;
    string country;
    int zip?;
    int houseNo?;
    json...;
|};

type Friend record {|
    string firstName;
    string lastName;
    Address address;
    json...;
|};

type NewRecord record {|
    string firstName;
    string lastName;
    Address address;
    Friend friend;
    json...;
|};
