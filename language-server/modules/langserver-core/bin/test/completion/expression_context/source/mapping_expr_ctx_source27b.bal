type Address record {|
    string street;
    string city;
    string postalCode;
|};

type Person record {|
    string name;
    int age;
    Address address;
|};

function addPerson() {

}

public function func() {
    addPerson({});
}
