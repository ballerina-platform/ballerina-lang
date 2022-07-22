import ballerina/http;

@choreo:Service{id: "002"}
service /noon on new http:Listener(8080) {
    resource function get hello () returns string|error {
        @choreo:Client { serviceId: "003"}
        http:Client timeClient = check new("http://localhost:7070");
        string date = check timeClient->get("time/currentDate");
        return "Good Afternoon. Date is " + date;
    }
}
