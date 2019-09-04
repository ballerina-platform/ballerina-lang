
type Employee record {
    string name;
    int age;
    string status;
};

stream<Employee> employeeStream1 = new;
stream<Employee> employeeStream4 = new;

function testStreamingQuery() {

    int i = 1;

    forever {
        from employeeStream1
        where employeeStream1.age > 30 as x
        select x.name, x.age, x.status
        => (Employee[] emp) {
            println("Filterted event received #: ", i);
            foreach var e in emp {
                employeeStream4.publish(e);
            }
        }
    }
}

function println(any... names){
}

function foo() {
    stream<Employee> employeeStream2 = new;
}

function testTableQuery() {
    table<Employee> tbl = table {
                                    { key name, age, status },
                                    [ { "Mary", 25 , "P" },
                                      { "John", 30 , "T" },
                                      { "Jim", 27, "P" }
                                    ]
                                };
    table<Employee> copy = from tbl select *;
}

function testTransactionStmtWithCommitedAndAbortedBlocks() returns string {
    string a = "";
    int count = 0;
    transaction with retries=2 {
        a = a + " inTrx";
        a = a + " endTrx";
    } onretry {
        a = a + " retry";
    } committed {
        a = a + " committed";
    } aborted {
        a = a + " aborted";
    }
    a = (a + " end");
    return a;
}

function testXML() {
    xml bookXML = xml `<book>
    <name>Sherlock Holmes</name>
    <author>
        <fname title="Sir">Arthur</fname>
        <mname>Conan</mname>
        <lname>Doyle</lname>
    </author>
    <!--Price: $10-->
    </book>`;
    string? name = bookXML.author["fname"]@["title"];
}

int counter = 10;

function testLock() {
    lock {
        counter = counter + 1;
    }
}
