type PeopleItem record {|
    string firstName;
    string lastName;
    string gender;
    int age;
    string number;
    json...;
|};

type NewRecord record {|
    PeopleItem[] people;
    json[] addresses;
    json...;
|};
