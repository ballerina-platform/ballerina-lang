type NewRecord record {|
    record {|
        string country;

        record {|
            string code; string name; json...;
        |} city; string Lane;
        json...;
    |} address;

    string school;
    string name;
    int age;
    json...;
|};
