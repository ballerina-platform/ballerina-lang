type PeopleItem_01 record {|
    string firstName;
    string lastName;
    string gender;
    int age;
    string number;
    json...;
|};

type NewRecord_01 record {|
    PeopleItem_01[] people;
    json[] addresses;
    json...;
|};
