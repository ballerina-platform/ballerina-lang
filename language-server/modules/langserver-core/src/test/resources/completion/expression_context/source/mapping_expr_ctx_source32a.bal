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

class Employee {

    public function init(Person person) {

    }
}

public function func() returns error {
    Employee employee = new Employee({});
}
