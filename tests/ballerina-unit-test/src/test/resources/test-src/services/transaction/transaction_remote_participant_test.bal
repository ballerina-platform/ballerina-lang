import ballerina/http;
import ballerina/log;
import ballerina/transactions;


@http:ServiceConfig {
    basePath: "/"
}
service<http:Service> hello bind { port: 10234 } {
    @http:ResourceConfig {
        methods: ["POST"],
        path: "/",
        transactionInfectable: true
    }
    @transactions:Participant {
        oncommitFunc: baz,
        onabortFunc: bar
    }
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;

        res.setPayload("payload-from-remote");
        S1 = S1 + " in-remote";

        caller->respond(res) but { error e => log:printError(
                                                  "Error sending response", err = e) };
    }
}


string S1 = "";

function baz(string id) {
    S1 = S1 + " in-baz[oncommittedFunc]";
}

function bar(string id) {
    S1 = S1 + " in-bar[onabortFunc]";
}

boolean thrown1 = false;
boolean thrown2 = false;

function initiatorFunc(boolean throw1, boolean throw2) returns string {
    endpoint http:Client clientEP {
        url:"http://localhost:10234"
    };
    transaction with retries=2 {
        S1 = S1 + " in-trx-block";
        http:Request req = new;
        var resp = clientEP->post("/", req);
        match (resp) {
            http:Response res => match (res.getTextPayload()) {
                string r => {
                                log:printInfo(r);
                                S1 = S1 + " <" + r + ">";
                            }
                error err => log:printError(err.message);
            }
            error err => log:printError(err.message);
        }
        if (throw1 && !thrown1) {
            thrown1 = true;
            int blowNum = blowUp();
        }
        if (throw2 && !thrown2) {
            thrown2 = true;
            int blowNum = blowUp();
        }
        S1 = S1 + " in-trx-lastline";
    } onretry {
        S1 = S1 + " onretry-block";
    } committed {
        S1 = S1 + " committed-block";
    } aborted {
        S1 = S1 + " aborted-block";
    }
    S1 = S1 + " after-trx";
    return S1;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = { message: "TransactionError" };
        throw err;
    }
    return 5;
}
