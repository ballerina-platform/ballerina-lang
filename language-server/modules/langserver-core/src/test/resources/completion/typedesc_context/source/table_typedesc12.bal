type Student record {
    readonly string name;
    int age;
};
type Employee record {
    readonly string name;
    float salary;
};
type Person Student|Employee;

type PersonTable table<Person> 
