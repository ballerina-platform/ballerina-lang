
int resValue = 0;
string stringResVal = "";

public function passValueForDefaultableObjectField () returns [int, string] {
    Person p = new Person(age = 50, name = "passed in name value");
    return [resValue, stringResVal];
}

class Person {
    public int age;
    public string name;

    function init (int age = 10, string name = "sample result") {
        self.age = age;
        self.name = name;
        resValue = age;
        stringResVal = name;
    }
}

