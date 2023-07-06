type SportsItem record {|
    string? sport;
    string position?;
    boolean reserve?;
    string game?;
    string 'type?;
|};

type Author record {|
    string name;
    string country;
    (boolean|int|string)? period;
    anydata language?;
|};

type BooksItem record {|
    string name;
    Author author;
    int publishedYear?;
    decimal price?;
|};

type State record {|
    string name;
    string code;
|};

type Address record {|
    string number;
    string street;
    string neighborhood;
    string city;
    State state;
|};

type NewRecord record {|
    string name;
    string school;
    int age;
    SportsItem[] sports;
    BooksItem[] books;
    string year;
    boolean honors;
    Address address;
|};
