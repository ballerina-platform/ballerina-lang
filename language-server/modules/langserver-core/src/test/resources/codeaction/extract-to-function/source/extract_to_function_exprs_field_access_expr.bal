class Student {
    private int age = 10;
    public boolean isAsian = true;

    public function getAge() returns int {
        return self.age;
    }

    public function setAge(int age) {
        self.age = age;
    }
}

type Person record {
    int age;
};

function testFunction() {
    Student kamal = new ();
    Person nimal = {age: 10};

    int kamals_age = kamal.getAge();
    int nimals_age = nimal.age;

    boolean isAsian = kamal.isAsian;
    nimal.age = 11;
}
