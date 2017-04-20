import ballerina.lang.strings;

struct homeAddress {
	string no;
	string lane;
	string district;
	string province;
	string country;

}
struct address {
	string number;
	string street;
	string district;
	string state;

}
struct employee {
	string firstName;
	string secondName;
	homeAddress location;

}
struct people {
	string name;
	string country;
	address location;

}

typemapper homeAddressToAddress(homeAddress y)(address) {
	address x = {};
	x.street = y.lane;
	x.district = y.district;
	x.state = y.province;
	x.number = y.no;
	return x;

}

typemapper employeeToPeople(employee y)(people) {
	people x = {};
	x.name = strings:trim(y.firstName);
	x.location = (address)y.location;
	return x;

}