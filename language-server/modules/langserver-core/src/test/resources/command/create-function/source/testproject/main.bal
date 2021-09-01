import testproject.module1;

type School record {
    module1:Student[] students;
};

public function main() {
    string name = "John Doe";
    module1:Student student = createStudent(name, age);
    addStudent(student);
    module1:Student converted = convertStudent(student);
    
    module1:Student studentRecord = {
        name: name,
        age: getAge(student)
    };

    generateIndex(module1:intake, name);
}
