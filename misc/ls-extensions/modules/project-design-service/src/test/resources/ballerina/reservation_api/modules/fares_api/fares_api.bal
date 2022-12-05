
import ballerina/http;

public type Fare record {
    string flightNo;
    string flightDate;
    float rate;
};

@display {
    label: "",
    id: "003"
}
service /fares on new http:Listener(9090) {
    
    // send seat fare of the given flight.
    resource function get fare/[string flightNumber]/[string flightDate]() returns Fare|error {
        Fare fare = {flightNo: flightNumber, flightDate: flightDate, rate: 300.00};
        return fare;
    }
}