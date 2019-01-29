
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

function testTransactionStmtWithCommitedAndAbortedBlocks() {
    int failureCutOff = 3;
    boolean requestAbort = false;
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff;
    int count = 0;
    transaction with retries=2 {
        a = a + " inTrx";
        count = count + 1;
        if (count <= failureCutOff) {
            a = a + " blowUp";
            int bV = blowUp();
        }

        if (requestAbort) {
            a = a + " aborting";
            abort;
        }
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
