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

public function main() {
    Student Kamal = {
        name : "Kamal",
        age : 10,
        grades : {
            maths : 90,
            physics : 99,
            chemistry : 95
        },
        city : "Colombo"
    };

    Student Amal = {
            name : "Amal",
            age : 10,
            grades : {
                maths : 98,
                physics : 100,
                chemistry : 95
            },
            city : "Galle"
        };

    Grades Kamal_grades = mapStudentToGrades(Kamal);

    Grades Amal_grades = Amal;
}



function mapStudentToGrades (Student student) returns Grades {
// Some record fields might be missing in the AI based mapping.
	Grades grades = {maths: student.grades.maths, chemistry: student.grades.chemistry, physics: student.grades.physics};
	return grades;
}
