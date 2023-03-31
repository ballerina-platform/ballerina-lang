
class Student {
    private string name;

    public function init(string name) {
        self.name = name;
    }
}

function test() {
    Student s = createStudent();
    Student[] students = [s];
    ClassRoom classRoom = createClassRoom(students);

    Address address = createAddress("Sri Lanka");
}

class ClassRoom {
    private string className;
    private int grade;
    private Student[] students;

    public function init(string className, int grade, Student[] students) {
        self.className = className;
        self.grade = grade;
        self.students = students;
    }
}

public type Address object {
    public string city;
    public string country;
    public function value() returns string;
};
