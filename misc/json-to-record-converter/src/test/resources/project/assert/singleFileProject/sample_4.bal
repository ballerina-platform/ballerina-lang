type PeopleItem_01 record {
    string firstName;
    string lastName;
    string gender;
    int age;
    string number;
};

type NewRecord_01 record {
    PeopleItem_01[] people;
    anydata[] addresses;
};
