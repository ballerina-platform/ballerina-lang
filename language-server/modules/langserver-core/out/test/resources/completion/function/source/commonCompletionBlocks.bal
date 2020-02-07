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

function initiateNestedTransactionInRemote(string nestingMethod) returns @tainted string {
   http:Client remoteEp = new("http://localhost:8889");
    string s = "";
    transaction {
        s += " in initiator-trx";
        
        // this call sends the transaction context with it
        var resp = remoteEp->post("/nestedTrx", nestingMethod);
        if (resp is http:Response) {
            if (resp.statusCode == 500) {
                s += " remote1-excepted";
                var payload = resp.getTextPayload();
                if (payload is string) {
                    s += ":[" + payload + "]";
                }
            } else {
                var text = resp.getTextPayload();
                if (text is string) {
                    log:printInfo(text);
                    s += " <" + text + ">";
                } else {
                    s += " error-in-remote-response " + text.reason();
                    log:printError(text.reason());
                }
            }
        } else {
            s += " remote call error: " + resp.reason();
        }
    } onretry {
        s += " onretry";
        
    } committed {
        s += " committed";

    } aborted {
        s += " aborted";

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

