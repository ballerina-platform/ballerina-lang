type PeopleItem record {
	string firstName;
	string lastName;
	string number;
	string gender;
	int age;
};

type NewRecord record {
	PeopleItem[] people;
};

