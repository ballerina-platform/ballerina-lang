
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
