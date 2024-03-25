import ballerina/http;

public type Student readonly & record {
    string name;
    int age;
};

http:Listener httpListener = check new (9090);

public service class StudentService {
    *http:Service;

    Student[] students = [];

    resource function get students() returns Student[] {
        return self.students;
    }

    resource function post student(Student student) {
        self.students.push(student);
    }

    resource function post students(Student[] students) {
        self.students.push(...students);
    }
}
