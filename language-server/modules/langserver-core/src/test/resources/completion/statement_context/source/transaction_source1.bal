import ballerina/module1 as mod;

public function main() {
    transaction {
        var res = getError();
        if(res is error) {
            
        } else {
            checkpanic commit;
        }
    }
}

function getError() returns error? {
    return error("Custom error");
}
