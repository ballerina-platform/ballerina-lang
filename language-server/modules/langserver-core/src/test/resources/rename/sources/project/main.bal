import project.module1;
import project.module2;

public function main() {
    module1:School school = new module1:School();

    module2:Person[] people = module2:getPeople();

    foreach module2:Person person in people {
        if (person.getAge() <= 18) {
            school.addStudent(person);
        }
    }

    int localInt = module1:gInt;

    module2:setName(fname = "John", lname = "Doe");
    
    stream<module2:Human> s = new;
    table<module2:Human> tHuman = table [{id: 1, name: "Jane"}];
}
