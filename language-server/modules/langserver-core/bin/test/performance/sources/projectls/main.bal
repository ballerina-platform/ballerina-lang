import projectls.module1;
public function main() {
    module1:Student s1 = {
        firstName: "Mary", 
        lastName: "Campbel", 
        intakeYear: 2010, 
        gpa: 2.9
    };

    module1:Student s2 = {
        firstName: "Joseph",
        lastName: "Sterling",
        intakeYear: 2010,
        gpa: 2.010
    };

    module1:Student[] sArray = [s1, s2];

    Report[] listResult = from var student in sArray
            where student.gpa >= 2.1
            let string degreeName = "Bachelor of Medicine",
            int graduationYear = 2015
            order by student.firstName ascending
            limit 2
            select {
                name: student.firstName + " " + student.lastName,
                degree: degreeName,
                graduationYear: graduationYear
            };
    listResult.
}

public type Report record {
    string name;
    string degree;
    int graduationYear;
};
