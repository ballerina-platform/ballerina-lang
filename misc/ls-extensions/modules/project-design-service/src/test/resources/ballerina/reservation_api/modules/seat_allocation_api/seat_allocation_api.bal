import ballerina/http;

@display {
    label: "",
    id: "002"
}service /allocation on new http:Listener(8080) {
    resource function get flights/[string flightNumber](string? flightDate) returns Flight[] {
        if flightDate is () {
            return findByFlightNumber(flightNumber);
        } else {
            Flight? found = find(flightNumber, flightDate);
            if found is () {
                return [];
            } else {
                return [found];
            }
        }
    }

    resource function post my/flights(@http:Payload SeatAllocation payload) returns SeatAllocation |error {
        return addReservation(payload);
    }
    
}



