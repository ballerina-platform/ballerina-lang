type NewRecord record {
    string firstName;
    string lastName;
    string gender;
    int age;
    record {string streetAddress; string city; string state;} address;
    (decimal|int|string|(int|string)[]|anydata[]|string[]|(int|string)[][])[] testList;
};
