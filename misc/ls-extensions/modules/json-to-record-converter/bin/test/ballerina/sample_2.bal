type Address record {
	string streetAddress;
	string city;
	string state;
};

type PhoneNumbersItem record {
	string number;
	string 'type;
};

type NewRecord record {
	string firstName;
	string lastName;
	Address address;
	string gender;
	int age;
	PhoneNumbersItem[] phoneNumbers;
};

