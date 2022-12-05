import ballerina/http;

enum Time {
    morning,
    afternoon,
    night
}

@display {
    label: "greeting-service",
    id: "001"
}
service /greeting on new http:Listener(8080) {
    resource function get . (string name, string time) returns string|error {
        @display {
            label: "time-service",
            id: "002"
        }
        http:Client timeService = check new("https://localhost:9090");
        string greeting = check timeService->/["greeting"].get(param = time);
        return string `Hello ${name}. ${greeting}`;
    }
}

@display {
    label: "time-service",
    id: "002"
}
service /time on new http:Listener(9090) {
    resource function get greeting(string time) returns string {
        match time {
            morning => {
                return "Welcome to morning show !";
            }
            afternoon => {
                return "Welcome to evening show !";
            }
            night => {
                return "Welcome to late night show !";
            }
            _ => {
                return "Welcome to the show !";
            }
        }
        
    }
}