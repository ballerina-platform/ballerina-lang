import ballerina/log;
import ballerina/http;

listener http:Listener x = new (9090);

@choreo:Service{id: "001"}
service /greeting on x {
    resource function get name() returns error? {
        log:printInfo("Hello World");
        @choreo:Client{serviceId: "002"}
        http:Client seat_allocation_client = check new (seatAllocationAPIUrl);
        Flight[] flights = check seat_allocation_client->get(string `/flights/${payload.flightNumber}?${payload.flightDate}`);
    }
}