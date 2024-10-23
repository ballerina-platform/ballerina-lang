type NewRecord record {|
    string firstName;
    string lastName;
    record {|
        string city;
        string country;
        int zip?;
        int houseNo?;
        json...;
    |} address;
    record {|
        string firstName;
        string lastName;
        record {|
            string city;
            string country;
            int zip?;
            int houseNo?;
            json...;
        |} address;
        json...;
    |} friend;
    json...;
|};
