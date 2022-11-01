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

function addPerson(Person person) {

}

public function testFunction() {
    addPerson({});
}
