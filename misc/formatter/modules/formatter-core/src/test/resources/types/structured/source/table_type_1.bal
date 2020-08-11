type Person record {|
   readonly int id;
   string name;
|};
type Employee record {
   readonly int id;
   string name;
};
type EmployeeTable   table
<  Employee  >    key  ( id  )  ;type
CustomerTable
table   < map  <  any  >  >
;

public function foo() {
   EmployeeTable employeeTab =
   table
   [
     {id: 1, name: "John"}
   ];table   <Person> personTab = employeeTab.'map(function (Employee employee) returns Person {
       return {id: employee.id, name:employee.name};
   });

   CustomerTable customerTab =table[{id: 13 , fname: "Dan"}];

   var studentTab =      table
   [{id: 44, fname: "Meena"}
   ]
   ;
}


