type Employee record {|
    string name;
    string empId;
    string email;
|};

type Person record {|
    string name;
    string email;
|};

type Admission record {
    string empId;
    string admissionDate;
};

function transform(Person person, Admission admission) returns Employee => {
    name: person.name,
    empId: admission.empId,
    email: person.email
};
