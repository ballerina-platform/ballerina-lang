import ballerina/io;
import ballerina/http;
import ballerina/log;
















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
}

service sampleService on new http:Listener(8080) {




    resource function sampleResource(http:Caller caller, http:Request request) {
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
        error err = error("Division by 0 is not defined");
        return err;
    }
    return a / b;
}

function initiateNestedTransactionInRemote(string nestingMethod, int failureCutOff, boolean requestRollback) returns @tainted string {
   http:Client remoteEp = new("http://localhost:8889");
    string s = "";
    transaction {
            s = s + " inTrx";
            count = count + 1;
            if (transactional) {
                int booooo = 12;
            }
            if (count <= failureCutOff) {
                s = s + " blowUp"; // transaction block panic scenario, Set failure cutoff to 0, for not blowing up.
                int bV = blowUp();
            }
            if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
                s = s + " Rollback";
                
                rollback;
            } else {
                a = a + " Commit";
                var i = commit;
            }
            s = s + " endTrx";
            s = (s + " end");
        }
    return s;
}

function functionForkJoin() returns int{
    int x = 5;

    fork {
        worker w1 {
            int a = 5;
            int b = 0;
            a -> w2;
            b  = <- w2;
        }

        worker w2 {
            int a = 0;
            int b = 15;

            a = <- w1;
            b -> w1;
        }
    }
    worker wx returns int {
       int y = 50;
       return y + 1;
    }

    return (wait wx) + 1;
}

