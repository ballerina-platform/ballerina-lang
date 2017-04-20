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

typemapper foo(homeAddress y)(address){
	address x = {};
	x.street = y.lane;
	x.district = y.district;
	x.state = y.province;
	x.number = y.no;
	return x;
}