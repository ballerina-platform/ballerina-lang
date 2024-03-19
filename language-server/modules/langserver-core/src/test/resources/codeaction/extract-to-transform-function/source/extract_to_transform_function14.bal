import ballerina/module1;

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
    module1:TestRecord1 testRecord1;
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

function transform(Person person, Admission admission, module1:TestRecord1 testRecord1) returns Employee => {
    name: person.name,
    empId: admission.empId,
    email: person.email,
    testRecord1: testRecord1
};
