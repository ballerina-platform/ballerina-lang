public function main() {
    int ans = intAdd(5, 3);
}

//
// Object Definition
//
type Person object {
    public string firstName;
    public string lastName;

    function __init(string firstName, string lastName) {
        self.firstName = firstName;
        self.lastName = lastName;
    }

    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }
};


//
// FUNCTIONS
//

// This should be mocked
public function intAdd(int a, int b) returns (int) {
    return a+b;
}

// This should NOT be mocked
public function intSubtract(int a, int b) returns (int) {
    return a-b;
}
