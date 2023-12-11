Person[] people = [];
boolean initialized = false;

# Person  
public class Person {
    private string name;
    private int age;
    private string identificationNo;

    public function init(string name, int age, string identificationNo) {
        self.name = name;
        self.age = age;
        self.identificationNo = identificationNo;
    }

    public function getName() returns string {
        return self.name;
    }

    public function getAge() returns int {
        return self.age;
    }

    public function getIdentificationNo() returns string {
        return self.identificationNo;
    }
}

# Get the list of available people
# + return - People  
public function getPeople() returns Person[] {
    if (!initialized) {
        people.push(new Person("John Doe", 25, "453332212N"));
        people.push(new Person("Harrison Ford", 15, "123442212N"));
        people.push(new Person("Steve Smith", 25, "21212333N"));
        people.push(new Person("Johny Cage", 14, "553332212N"));
        people.push(new Person("Frank", 28, "909888212N"));
        people.push(new Person("Anne", 11, "389089212N"));
    }

    return people;
}

# Adds a new person 
#
# + p - Person to be added 
public function addPerson(Person p) {
    people.push(p);
}

# Directory path
public string directoryPath = "/";

string name = "";

# Set name.
#
# + fname - parameter description
# + lname - parameter description
public function setName(string fname, string lname) {
    name = fname + " " + lname;
}
