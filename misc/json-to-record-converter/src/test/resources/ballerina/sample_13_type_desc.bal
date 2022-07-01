type NewRecord record {
    int salary;
    record {string streetAddress; string city; string state;} address;
    record {string name; record {string streetAddress; string city; string state?;} address;} school;
    record {string name; record {string city; string state?; string streetAddress?;} address;} work;
};
