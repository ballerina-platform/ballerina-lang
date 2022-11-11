type People_01Item record {
    string firstName;
    string lastName;
    string gender;
    int age;
    string number;
};

type NewRecord_01 record {
    People_01Item[] people;
    anydata[] addresses;
};
