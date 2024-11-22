type Address record {|
    string houseNo;
    string line1;
    string line2;
    string city;
    string country;
|};

type Employee record {|
    string name;
    string empId;
    string email;
    Address address;
|};

type Person record {|
    string name;
    string email;
    Address address;
|};

type Admission record {
    string empId;
    string admissionDate;
};

function transform(Person person, Admission admission) returns Employee =>
    let Address address = {country: "", city: "", houseNo: "", line2: "", line1: ""} in {
        name: person.name,
        empId: admission.empId,
        email: person.email,
        address: address
    };
