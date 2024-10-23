type Author record {|
	string country;
	string period;
	string name;
	json...;
|};

type BooksItem record {|
	Author author;
	string name;
	json...;
|};

type State record {|
	string code;
	string name;
	json...;
|};

type Address record {|
	string number;
	string city;
	string street;
	string neighborhood;
	State state;
	json...;
|};

type SportsItem record {|
	string position;
	string sport;
	json...;
|};

type NewRecord record {|
	BooksItem[] books;
	Address address;
	SportsItem[] sports;
	string school;
	string year;
	string name;
	int age;
	boolean honors;
	json...;
|};

