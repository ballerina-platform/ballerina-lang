type Address record {|
	string streetAddress;
	string city;
	string state;
    json...;
|};

type PhoneNumbersItem record {|
	string number;
	string 'type;
    json...;
|};

type NewRecord record {|
	string firstName;
	string lastName;
	Address address;
	string gender;
	int age;
	PhoneNumbersItem[] phoneNumbers;
    json...;
|};

