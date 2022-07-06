type NewRecord record {
    string name;
    string school;
    int age;
    record {record {string name; string code;} city; string country; string lane; anydata zip?;} address;
    anydata phoneNumber?;
};
