import ballerina/lang.'object as lang_obj;

class Person {
    string name = "";
    int age = 10;
}

public function testLangLibFuncInvocationNegative() {
    Person person = new;
    string str = person.toString();
}

class Employee {
    string name = "";
    int age = 10;

    public function toString() returns string {
        return "Name:" + self.name + " age:" + self.age.toString();
    }
}

public function testObjectMethodInvocation() {
    Employee emp = new;
    string str = emp.toString();
}

public type Frame readonly & object {
   public function returnString() returns string;
};

public readonly class FrameImpl {
   *Frame;
}

public class RawTemplateImpl {
    *lang_obj:RawTemplate;

    public function init() {
        self.insertions = [1, 2, 3];
    }
}
