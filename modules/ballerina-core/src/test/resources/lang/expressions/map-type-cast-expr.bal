 function mapPrimitiveCastTest(int input) (double) {
    map ints = {"first":input};
    double doubleVal = (double) ints["first"];
    return doubleVal;
 }

 function mapStructCastTest(string name1, string name2) (string) {
  	Person person1 = {name:name1};
  	Person person2 = {name:name2};
 	map people = {"person1":person1, "person2":person2};
 	Employee employee = (Employee) people["person1"];
 	return employee.fname;
 }

 function mapCastWithExprTest(int input) (string) {
    map ints = {"first":input};
    string output = "output" + (string) ints["first"];
    return output;
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