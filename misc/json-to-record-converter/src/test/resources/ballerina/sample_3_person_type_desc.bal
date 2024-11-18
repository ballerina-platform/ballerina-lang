type Person record {|
    string firstName;
    string lastName;
    string gender;
    int age;
    record {|
        (int|string) streetAddress;
        string city;
        string state;
        json...;
    |} address;
    json phoneNumber;
    record {|
        string firstName;
        string lastName;
        record {|
            (int|string) streetAddress;
            string city;
            string state;
            json...;
        |} address;
        json...;
    |} friend;
    json...;
|};
