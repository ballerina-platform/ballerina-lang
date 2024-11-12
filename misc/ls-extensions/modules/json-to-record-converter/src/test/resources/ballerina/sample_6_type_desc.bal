type Person record {|
    record {|
        record {|
            string country;
            string period;
            string name;
            json...;
        |} author;
        string name;
        json...;
    |}[] books;

    record {|
        string number;
        string city;
        string street;
        string neighborhood;
        record {|
            string code;
            string name;
            json...;
        |} state;
        json...;
    |} address;

    record {|
        string position;
        string sport;
        json...;
    |}[] sports;

    string school;
    string year;
    string name;
    int age;
    boolean honors;
    json...;
|};
