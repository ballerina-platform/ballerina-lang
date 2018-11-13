import ballerina/io;
import ballerina/http;
import ballerina/h2;

endpoint h2:Client testDB {
    path: "./local-transactions",
    name: "Testdb",
    username: "root",
    password: "root"
};

public function main(string... args) {
    int a = 10;
    int b = 20;
    int checkValue = 20;
    
    if (checkValue == a) {
        
        io:println("Value is 10");
    } else if (checkValue == 20) {
        
        io:println("Value is 20");
    } else {
        
        io:println("Value is Other");
    }
    
    while(a > 5) {
        
        io:println(a);
        a -= 1;
    }


    try {
        io:println("Start dividing numbers");

        a = check divideNumbers(1, 0);

    } catch (error err) {
        
        io:println("Error occurred: ", err.message);
        
        throw err;
    } finally {

        io:println("Finally block executed");
    }
}

service<http:Service> sampleService bind { port: 9090 } {
    int serviceVar1 = 124;

    string serviceVar2 = "Test String";

    sampleResource (endpoint caller, http:Request request) {
        worker w1 {
            int a = 12;
        }
        worker w2 {
            
            int b = 10;
        }
    }
}

function divideNumbers(int a, int b) returns int|error {
    if (b == 0) {
        error err = { message: "Division by 0 is not defined" };
        return err;
    }
    return a / b;
}

function transactionFunc(string... args) {
    var ret = testDB->update("CREATE TABLE CUSTOMER (ID INTEGER, NAME VARCHAR(30))");

    ret = testDB->update("CREATE TABLE SALARY (ID INTEGER, MON_SALARY FLOAT)");

    transaction with retries = 4, oncommit = onCommitFunction, onabort = onAbortFunction {
    
        var result = testDB->update("INSERT INTO CUSTOMER(ID,NAME) VALUES (1, 'Anne')");
                                     
        result = testDB->update("INSERT INTO SALARY (ID, MON_SALARY) VALUES (1, 2500)");
        match result {
            int c => {
                io:println("Inserted count: " + c);
                if (c == 0) {
                    
                    abort;
                }
            }
            error err => {

                retry;
            }
        }
    } onretry {
        
        io:println("Retrying transaction");
    }

    ret = testDB->update("DROP TABLE CUSTOMER");
}

function onCommitFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " committed");
}

function onAbortFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " aborted");
}

function functionForkJoin(string... args) {
    fork {
        worker w1 {
            int i = 23;
            string s = "Colombo";
            io:println("[w1] i: ", i, " s: ", s);
            
            (i, s) -> fork;
        }

        worker w2 {
            float f = 10.344;
            io:println("[w2] f: ", f);
            f -> fork;
        }
    } join (all) (map results) {
        int iW1;
        string sW1;
        (iW1, sW1) = check <(int, string)>results["w1"];
        io:println("[join-block] iW1: ", iW1, " sW1: ", sW1);
        float fW2 = check <float>results["w2"];
        
        io:println("[join-block] fW2: ", fW2);
    } timeout (1000) (map results) {
        
        if (results["w1"] != null) {
            int iW1;
            string sW1;
            (iW1, sW1) = check <(int, string)>results["w1"];
            io:println("[timeout-block] iW1: ", iW1, " sW1: ", sW1);
        }
     }
}
