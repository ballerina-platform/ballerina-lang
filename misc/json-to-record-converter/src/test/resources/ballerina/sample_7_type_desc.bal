type NewRecord record {
    string name;
    string school;
    int age;
    record {record {string name; string code;} city; string country; string lane; any zip?;} address;
    any phoneNumber?;
};
