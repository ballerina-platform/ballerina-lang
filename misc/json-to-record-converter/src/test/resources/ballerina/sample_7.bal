type City record {
    string name;
    string code;
};

type Address record {
    City city;
    string country;
    string lane;
    any zip?;
};

type NewRecord record {
    string name;
    string school;
    int age;
    Address address;
    any phoneNumber?;
};
