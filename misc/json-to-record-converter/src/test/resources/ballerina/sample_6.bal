type SportsItem record {
    string sport;
	string position;
};

type Author record {
    string name;
	string country;
	string period;
};

type BooksItem record {
    string name;
	Author author;
};

type State record {
    string name;
	string code;
};

type Address record {
	string number;
	string street;
	string neighborhood;
	string city;
	State state;
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
