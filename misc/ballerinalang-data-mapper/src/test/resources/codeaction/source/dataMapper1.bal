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

    Grades Kamal_grades = Kamal;
}
