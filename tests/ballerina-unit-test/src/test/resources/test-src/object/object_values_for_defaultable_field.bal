
int resValue = 0;
string stringResVal = "";

public function passValueForDefaultableObjectField () returns (int, string) {
    Person p = new Person(age = 50, name = "passed in name value");
    return (resValue, stringResVal);
}

type Person object {
    public int age;
    public string name;

    __init (age = 10, name = "sample result") {
        resValue = age;
        stringResVal = name;
    }
};

