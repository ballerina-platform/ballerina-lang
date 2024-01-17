type SportsItem record {
    string? sport;
    string position?;
    boolean reserve?;
    string game?;
    string 'type?;
};

type Author_01 record {
    string name;
    string country;
    (boolean|int|string)? period;
    anydata language?;
};

type BooksItem record {
    string name;
    Author_01 author;
    int publishedYear?;
    decimal price?;
};

type State_01 record {
    string name;
    string code;
};

type Address record {
    string number;
    string street;
    string neighborhood;
    string city;
    State_01 state;
};

type NewRecord record {
    string name;
    string school;
    int age;
    SportsItem[] sports;
    BooksItem[] books;
    string year;
    boolean honors;
    Address address;
};
