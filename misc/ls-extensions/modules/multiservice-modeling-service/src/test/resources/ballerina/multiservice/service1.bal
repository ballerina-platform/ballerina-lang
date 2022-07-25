import ballerina/log;
import ballerina/http;

public enum GreetingTime {
    MORNING,
    NOON,
    NIGHT
}

@choreo:Service{id: "001"}
service /greeting on new http:Listener(9090) {
    @choreo:Client { serviceId: "002"}
    http:Client noonClient = check new("http://localhost:8080");
    resource function get hello (string greetingTime) returns error? {
        if (greetingTime.equalsIgnoreCaseAscii(NOON)) {
            string greeting = check noonClient->get("/noon/hello");
            log:printInfo(greeting);
        }
    } 
}