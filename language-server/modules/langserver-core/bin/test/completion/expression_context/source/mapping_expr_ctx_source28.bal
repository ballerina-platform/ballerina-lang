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

function addPerson(string id, Person person, int timestamp) {

}

public function func() {
    addPerson(id = "123", person = {});
}
