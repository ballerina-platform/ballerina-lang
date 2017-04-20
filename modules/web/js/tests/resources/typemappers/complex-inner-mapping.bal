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

typemapper employeeToPeopleInner(employee y)(people) {
	people x = {};
	x.location.number = y.location.no;
	x.location.street = y.location.lane;
	x.location.district = y.location.district;
	x.location.state = y.location.province;
	x.name = y.firstName;
	x.country = y.location.country;
	return x;

}