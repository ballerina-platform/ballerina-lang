public type Person record {|
    string name;
    int age;
|};

type Employee record {|
    *Person;
    string designation;
|};

public class PersonClass {
    string name;
    int age;

    public function init(string name, int age) {
        self.name = name;
        self.age = age;
    }

    public function getName() returns string => self.name;

    public function getAge() returns int => self.age;
}

const ABC = "abc";

public type Digit 0|1|2|3|4|5|6|7|8|9;

public type ExampleErr distinct error;
