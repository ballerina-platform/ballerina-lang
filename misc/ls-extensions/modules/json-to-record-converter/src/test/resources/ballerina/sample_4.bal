type PeopleItem record {|
	string firstName;
	string lastName;
	string number;
	string gender;
	int age;
	json...;
|};

type NewRecord record {|
	PeopleItem[] people;
	json...;
|};

