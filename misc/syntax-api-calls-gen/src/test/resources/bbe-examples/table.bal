import ballerina/io;

type Person record {|
    readonly int id;
    string name;
    int age;
|};

// This is the `type` created to represent a data row.
// The type of the value of the ID field is `readonly` and the field itself cannot be mutated.
// The ID is the unique identifier of an Employee.
type Employee record {
    readonly int id;
    string name;
    float salary;
};

// This is a `table` type.
// The `EmployeeTable` type has members that are employees, and within the `EmployeeTable`, each member is
// uniquely identified by its ID field.
type EmployeeTable table<Employee> key(id);

// You can define a `table` value whose members are of type `map` constrained by `any` type.
type CustomerTable table<map<any>>;

public function main() {

    // A `table` constructor works like a `list` constructor.
    // The table preserves the order of its members. Iterating over the table gives the
    // members of the table sequentially.
    EmployeeTable employeeTab = table [
      {id: 1, name: "John", salary: 300.50},
      {id: 2, name: "Bella", salary: 500.50},
      {id: 3, name: "Peter", salary: 750.0}
    ];

    // Prints the `table` data.
    io:println("Employee Table Information: ", employeeTab);

    // Returns the number of members of a `table`.
    io:println("Total number of Employees: ", employeeTab.length());

    // Adds a new member to the `Employee` table.
    Employee emp = {id: 4, name: "Max", salary: 900.0};
    employeeTab.add(emp);

    // Retrieves a member using the ID field of an Employee.
    io:println("New Employee: ", employeeTab[4]);

    // Returns the member of a `table` with a given key.
    io:println("Employee 1: ", employeeTab.get(1));

    // Removes the member of a `table` with a given key and returns it.
    io:println("Information of the removed Employee: ", employeeTab.remove(2));

    // Gives a list of all keys of a `table`.
    var personTblKeys = employeeTab.keys();
    io:println("Employee table keys: ", personTblKeys);

    // The members of a `table` can be returned as an `array`.
    Employee[] tableToList = employeeTab.toArray();
    io:println("Employee table to list: ", tableToList);

    // Tables are an iterable type and tables support functional iteration operations such as `.forEach()`,
    // `.map()`, `.filter()`, and `.reduce()`.
    string filtered = "";
    employeeTab.forEach(function (Employee employee) {
           if (employee.salary < 400.0) {
              filtered += employee.name;
              filtered += " ";
           }
    });
    io:println("Employees with salary < 400.0: ", filtered);

    // `.map()` applies a function to each member of a table and returns a table of the result.
    // The resulting table will have the same keys as the argument table.
    table<Person> personTab = employeeTab.'map(function (Employee employee)
                                                            returns Person {
        return {id: employee.id, name:employee.name, age:23};
    });
    io:println("Person Table Information: ", personTab);

    // Create `table` values with map-constrained members.
    CustomerTable customerTab = table [
        {id: 13 , fname: "Dan", lname: "Bing"},
        {id: 23 , fname: "Hay" , lname: "Kelsey"}
    ];
    io:println("Customer Table Information: ", customerTab);

    // The table constructor can be used without a contextually-expected type. Member access is not
    // allowed here.
    var studentTab = table [
        {id: 44, fname: "Meena", lname: "Kaur"},
        {id: 55, fname: "Jay", address: "Palm Grove, NY"}
    ];
   io:println("Student Table Information: ", studentTab);

}
