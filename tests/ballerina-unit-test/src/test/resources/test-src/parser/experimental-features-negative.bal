
type Employee record {
    string name;
    int age;
    string status;
};

stream<Employee> employeeStream1 = new;
channel<json> chn1 = new;

function testStreamingQuery() {

    int i = 1;

    forever {
        from teacherStream7
        where age > 30
        select name, age, status
        => (Employee[] emp) {
            io:println("Filterted event received #: "+ i);
            foreach var e in emp {
                employeeStream4.publish(e);
            }
        }
    }
}

function foo() {
    stream<Employee> employeeStream2 = new;
}

function testTableQuery() {
    table<Person> personTableCopy = from personTable select *;
}
