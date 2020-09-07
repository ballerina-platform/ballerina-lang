
public class Employee {
    public int age = 0;
    private string name = "";
    string email = "";

    public function getName() returns string {
        return self.name;
    }

    private function getAge() returns int {
        return self.age;
    }

    function getEmail() returns string {
        return self.email;
    }
}

public class SameFieldAndMethodObject {
    public int someInt = 13;
    public float someFloat = 1.1;

    public function someInt() returns int {
        self.someInt += 10;
        return self.someInt;
    }

    public function someFloat() returns float {
        return 2.2;
    }

    public function getInt() returns int {
        return self.someInt;
    }

    public function testFloat() returns [float, float] {
        float f1 = self.someFloat;
        float f2 = self.someFloat();
        return [f1, f2];
    }
}

public class TempCache {
    public int capacity;
    public int expiryTimeInMillis;
    public float evictionFactor;

    public function init(int expiryTimeInMillis = 900000, int capacity = 100, float evictionFactor = 0.25) {
        self.capacity = capacity;
        self.expiryTimeInMillis = expiryTimeInMillis;
        self.evictionFactor = evictionFactor;
    }
}
