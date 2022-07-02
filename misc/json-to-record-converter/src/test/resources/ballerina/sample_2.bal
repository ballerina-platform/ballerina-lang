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
    (decimal|int|string|(int|string)[]|any[]|string[]|(int|string)[][])[] testList;
};
