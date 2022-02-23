
class Student {
    string name;
    int id;

    # Student constructor
    #
    # + name - Name  
    # + id - Id 
    function init(string name, int id) {
        self.name = name;
        self.id = id;
    }


}

function doSomething() {
    Student st = new Student("", 1); 
}
