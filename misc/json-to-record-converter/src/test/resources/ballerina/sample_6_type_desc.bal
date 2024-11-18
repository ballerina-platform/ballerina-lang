type NewRecord record {|
    string name;
    string school;
    int age;
    record {|
        string? sport;
        string position?;
        boolean reserve?;
        string game?;
        string 'type?;
        json...;
    |}[] sports;
    record {|
        string name;
        record {|
            string name;
            string country;
            (boolean|int|string)? period;
            json language?;
            json...;
        |} author;
        int publishedYear?;
        decimal price?;
        json...;
    |}[] books;
    string year;
    boolean honors;
    record {|
        string number;
        string street;
        string neighborhood;
        string city;
        record {|
            string name;
            string code;
            json...;
        |} state;
        json...;
    |} address;
    json...;
|};
