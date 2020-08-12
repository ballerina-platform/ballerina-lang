import ballerina/io;
type Home record {
    int number;
    string lane;
    string birth_country;
};

type BoardingPlace record {
    int num;
    string lane;
    string country;
};

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
    Student john = {
        name: "John Doe",
        age: 17,
        grades: {
            maths: 80,
            physics: 75,
            chemistry: 65
        },
        city: "London"
    };
    io:println(john);

    io:println(john.name);

    io:println(john["name"]);

    io:println(john.grades.maths);

    Grades grades = { maths: 80, physics: 75, chemistry: 65, "english": 90 };
    io:println(grades);


    int? english = grades["english"];
    io:println(english);

    Home home_1 = {
        number: 12,
        lane: "2nd Lane",
        birth_country: "Sri Lanka"
    };

    BoardingPlace bp = home_1;
}
