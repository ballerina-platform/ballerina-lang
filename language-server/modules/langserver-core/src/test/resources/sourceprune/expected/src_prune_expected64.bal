import module1;

function getStringInPkg() returns (string){
    int a = 12;
    
    Person p1 = { id: 1, age: 30, salary: 300.50, name: "jane", married: true };
    Person p2 = { id: 2, age: 30, salary: 300.50, name: "anne", married: true };
    Person p3 = { id: 3, age: 30, salary: 300.50, name: "peter", married: true };
    
    table<Person> t1 = table {
        { key id, salary, key name, age, married } 
        
    };
    
    int b = 12;
}
