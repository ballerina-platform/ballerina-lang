type PeopleItem record {
	string firstName;
	string lastName;
	string gender;
	int age;
	string number;
};

type NewRecord record {
	(PeopleItem|decimal|int)[] people;
};
