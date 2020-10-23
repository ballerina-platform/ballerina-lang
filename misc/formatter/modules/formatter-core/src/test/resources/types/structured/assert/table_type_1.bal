type EmployeeTable table<Employee> key(id);

type CustomerTable table<map<any>>;

public function foo() {
    EmployeeTable employeeTab = table [{id: 1, name: "John"}];
    table<Person> personTab = employeeTab.'map(function(Employee employee) returns Person {
                                                   return {
                                                       id: employee.id,
                                                       name: employee.name
                                                   };
                                               });

    CustomerTable customerTab = table [{id: 13, fname: "Dan"}];

    CustomerTable fruitTab = table [
            {id: 1, name: "Apples"},
            {id: 2, name: "Oranges"},
            {id: 3, name: "Grapes"},
            {id: 4, name: "Mangoes"}
        ];

    var studentTab = table [{id: 44, fname: "Meena"}];
}
