class Student {
    string name;
    int age;

    public function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    public function getName() returns string {
        return self.name;
    }

    public function getAge() returns int {
        return self.age;
    }
}
