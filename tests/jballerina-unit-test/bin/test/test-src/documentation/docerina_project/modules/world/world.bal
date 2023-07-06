# Represents a person object.
#
# + name - Name of the person
# + age - Age of the person in years
# + address - Address of the person
# + wealth - Account balance of the person
public class PersonZ {
    public string name = "";
    public int age = 0;
    public string address = "";
    public float wealth = 0;

    # Gets invoked to initialize the `Person` object.
    #
    # + name - Name of the person for the constructor
    # + age - Age of the person for the constructor
    public function init(string name, int age) {
    }

    # Get the address of the person.
    #
    # + return - New address of the person
    public function getAddress() returns string {
        return self.address ;
    }

    # Add wealth of the person.
    #
    # + amt - Amount to be added
    # + rate - Interest rate
    public function addWealth(int[] amt, float rate=1.5) {
    }
}
