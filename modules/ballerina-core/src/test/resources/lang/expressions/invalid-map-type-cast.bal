 function invalidMapStructCastTest(string name1, string name2) (string) {
  	Employee employee1 = {fname:name1};
  	Employee employee2 = {fname:name2};
 	map employees = {"employee1":employee1, "employee2":employee2};
 	Person person = (Person) employees["employee1"];
 	return person.name;
 }

 struct Person {
    string name;
 }

 struct Employee {
    string fname;
 }

 typemapper personToEmployee(Person p)(Employee){
    Employee e = {};
    e.fname = p.name;
    return e;
 }