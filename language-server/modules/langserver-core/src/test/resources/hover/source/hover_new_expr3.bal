public function main() returns error? {
    Student val = check new ("", 1);
}

# A student class  
public class Student {
    string name;
    int id;

    # Student init function
    # 
    # + name - Name  
    # + id - Id 
    # + return - Returns an error if any occurs
    public function init(string name, int id) returns error? {
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
