type NewRecord record {
    string firstName;
    string lastName;
    string gender;
    int age;
    record {string streetAddress; string city; string state;} address;
    anydata phoneNumber;
    record {string firstName; string lastName; record {int streetAddress; string city; string state;} address;} friend;
};
