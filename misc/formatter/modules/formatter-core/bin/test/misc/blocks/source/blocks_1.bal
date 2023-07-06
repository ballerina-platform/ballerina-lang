type Student record {
            string name;
int age;    Grades grades;
};

type Address record {| string city;   string country;  |};

function foo() {
    Student john = {
            name: "John Doe",
            age,
            grades: {
                maths: 80,
                chemistry: 65
            }
        };

    Student john = {name: "John Doe",age,
            grades: {maths: 80,chemistry: 65}
            };

    Student john = {name: "John Doe",age,grades: {maths: 80,chemistry: 65}};

    Student john = {
        name: "John Doe",age,grades: {maths: 80,chemistry: 65}};
}
