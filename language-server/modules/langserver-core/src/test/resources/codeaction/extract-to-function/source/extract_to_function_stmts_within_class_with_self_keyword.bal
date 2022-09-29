type FullName record {
    string firstName = "asd";
    string lastName;
};

class Person {
    FullName name = {firstName: "", lastName: ""};
    int age = 0;

    public function init(FullName name, int age) {
        self.name.firstName = name.firstName;
        self.name.lastName = name.lastName;
        self.age += age;
    }

    public function doSomething() returns FullName {
        int aa = self.age;
        // self.age = 1;
        doSomeOtherThing(aa);
        return self.name;
    }
}

function doSomeOtherThing(int a) {
    
}
