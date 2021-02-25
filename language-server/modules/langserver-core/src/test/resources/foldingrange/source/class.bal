# Defines a `class` called `Employee`, which is structurally equivalent
# to the `Person` type.
class Employee {
    public int age;
    public string firstName;
    public string lastName;

    # Description
    #
    # + age - age Parameter Description
    # + firstName - firstName Parameter Description
    # + lastName - lastName Parameter Description
    function init(int age, string firstName, string lastName) {
        self.age = age;
        self.firstName = firstName;
        self.lastName = lastName;
    }

    # Description
    #
    # + return - Return Value Description
    function getFullName() returns string {
        return self.firstName + " " + self.lastName;
    }

    function checkAndModifyAge(int condition, int a) {
        if (self.age < condition) {
            self.age = a;
        }
    }
}
