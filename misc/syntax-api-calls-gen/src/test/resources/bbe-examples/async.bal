import ballerina/http;
import ballerina/io;
import ballerina/lang.'int;
public function main() {
    future<int> f1 = start calculate("365*24");

    int secondsInHour = 60 * 60;

    int hoursInYear = wait f1;
    int secondsInYear = hoursInYear * secondsInHour;
    io:println("Seconds in an year = ", secondsInYear);

    future<int> f2 = start calculate("125*34");
    future<int> f3 = start multiply(125, 34);
    int result1 = wait f2|f3;
    io:println("125*34 = ", result1);

    future<int> f4 = start calculate("9*8*7*6");
    future<int> f5 = start calculate("5*4*3*2*1");
    record { int r1; int r2; } result2 = wait {r1: f4, r2: f5};
    io:println("9! = ", result2.r1 * result2.r2);
}

function calculate(string expr) returns int {
    http:Client httpClient = new ("https://api.mathjs.org");
    var response = checkpanic httpClient->get(string `/v4/?expr=${expr}`);
    return checkpanic 'int:fromString(
        checkpanic <@untainted> response.getTextPayload());
}
function multiply(int a, int b) returns int {
    return a * b;
}