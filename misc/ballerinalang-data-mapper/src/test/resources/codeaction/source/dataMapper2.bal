type Address record {|
    string city;
    string[] country;
|};

type Grades record {|
    int maths;
    int physics;
    int chemistry;
    int...;
|};

type Student record {
    string name;
    int age;
    Grades grades;
    string city;
};

type Applicant record {
    Student applying_student;
    string school;
    string country;
};

public function main() {
    Student Kamal = {
        name: "Kamal",
        age: 10,
        grades: {
            maths: 90,
            physics: 99,
            chemistry: 95
        },
        city: "Colombo"
    };

    Applicant appl_1 = {
        applying_student: Kamal,
        school: "Royal College",
        country: "Sri Lanka"
    };

    Address student_address = appl_1;

}