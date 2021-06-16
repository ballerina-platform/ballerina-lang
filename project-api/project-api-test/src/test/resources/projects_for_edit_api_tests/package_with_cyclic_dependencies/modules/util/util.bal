import myproject.util; // this is to produce a cyclic dependency

public function concatStrings(string a, string b) returns string {
	return a + b;
}

public type PersonalDetails record {|
    string name;
    string contactNo;
|};
