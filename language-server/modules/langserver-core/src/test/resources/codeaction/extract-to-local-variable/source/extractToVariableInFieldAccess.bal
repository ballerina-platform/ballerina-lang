type Person record {
    string name;
    int age;
};

function testFunction() {
    Person p1 = {name: "Anne", age: 10};
    string name = p1.name;
}

class Employee {
    private int age = 40;
    public boolean isAsian = true;

    public function getAge() returns int {
        return self.age;
    }

    public function setAge(int age) {
        self.age = age;
    }
    
    public function testFieldAccess() {
        int years = 0;
        years = self.age;
    }
}

function testFunction2() {
     Employee emp = new();
     emp.setAge(23);
}