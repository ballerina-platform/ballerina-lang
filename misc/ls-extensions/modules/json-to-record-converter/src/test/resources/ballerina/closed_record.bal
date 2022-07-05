type City record {
    string code;
    string name;
};

type Address record {
    string country;
    City city;
    string Lane;
};

type NewRecord record {|
    Address address;
    string school;
    string name;
    int age;
|};
