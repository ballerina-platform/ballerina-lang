public function main() returns error? {
    Student st = new ("", 1);
}

# A student class  
public class Student {
    string name;
    int id;

    public function init(string name, int id) {
        self.name = name;
        self.id = id;
    }

    # Get Name
    # + return - Return the name of the student
    public function getName() returns string {
        return self.name;
    }

    # Get ID
    # + return - Return the ID
    public function getId() returns int {
        return self.id;
    }
}
