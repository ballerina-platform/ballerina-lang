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
    Address|error address;
|};

type Admission record {
    string empId;
    string admissionDate;
};

function transform(Person person, Admission admission) returns Employee|error => {
    name: person.name,
    empId: admission.empId,
    email: person.email,
    address: check person.address
};
