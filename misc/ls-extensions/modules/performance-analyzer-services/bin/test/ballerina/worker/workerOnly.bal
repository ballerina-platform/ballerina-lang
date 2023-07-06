import ballerina/http as http2;
import project.http as http;

service / on new http2:Listener(8090) {
    resource function get cases/[string shortCountryName]() returns int|error {
        decimal totalCases = 0;
        int population = 0;
        worker A returns decimal|error {
            http:Client clientEP = check new ("https://postman-echo.com/");
            json clientResponse = check clientEP->get("/");
            totalCases = check clientResponse.cases;
            return totalCases;
        }
        worker B returns int|error {
            http:Client clientEP = check new ("https://postman-echo.com/");
            json[] clientResponse = check clientEP->get("/");
            population = <int>check (clientResponse[0].value ?: 0) / 1000000;
            return population;
        }
        totalCases = check wait A;
        population = check wait B;
        return <int>(totalCases / population);
    }
}
