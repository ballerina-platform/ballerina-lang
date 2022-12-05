import ballerina/http;
import ballerina/log;
import reservation_api.seat_allocation_api as seat;
import reservation_api.fares_api as fares;

configurable string seatAllocationAPIUrl = "http://localhost:8080";
configurable string seatFareAPIUrl = "http://localhost:9090";

table<ConfirmedReservation> key(id) reservationInventory = table [];

listener http:Listener httpListener  = new http:Listener(9090);

@display {
    label: "",
    id: "001"
}
service /reservations/my on httpListener {
    resource function post reservation(@http:Payload Reservation payload) returns error|ConfirmedReservation {
        log:printInfo("Making a new Reservation: " + payload.toJsonString());

        @display {
            label: "",
            id: "002"
        }
        http:Client seat_allocation_client = check new (seatAllocationAPIUrl);
        string flightNumber = payload.flightNumber;
        string flightDate = payload.flightDate;

        Flight[] flights = check seat_allocation_client->/flights/[flightNumber].get(param = flightDate);

        if (flights.length() > 0) {
            http:Request bookingRequest = new;
            seat:SeatAllocation allocationDetails = {
                flightNumber: payload.flightNumber,
                flightDate: payload.flightDate,
                seats: payload.seats
            };

            bookingRequest.setPayload(allocationDetails);
            seat:SeatAllocation allocation = check seat_allocation_client->/my/flights.post(bookingRequest);

            @display {
                label: "",
                id: "003"
            }
            http:Client fare_client = check new (seatFareAPIUrl);
            fares:Fare fare = check fare_client->/fare/[flightNumber]/[flightDate].get;
            float totalFare = fare.rate * payload.seats;

            log:printInfo("Seat Allocation Details : " + allocation.toString() + "\n\n" + " Total Fare : " + totalFare.toString());

            ConfirmedReservation saved = saveReservation(payload, totalFare);
            return saved;
        }
        return error("Invalid flight");
    }

    resource function get reservation/[int reservationId](string name) returns ConfirmedReservation|error? {
        return reservationInventory[reservationId];
    }

    resource function get weather/[string country] () {

    }
}


function saveReservation(Reservation reservationRequest, float totalFare) returns ConfirmedReservation {
    ConfirmedReservation reservation = {
        id: reservationInventory.nextKey(),
        fare: totalFare,
        flightDate: reservationRequest.flightDate,
        origin: reservationRequest.origin,
        destination: reservationRequest.destination,
        bookingDate: currentDate(),
        flightNumber: reservationRequest.flightNumber,
        seats: reservationRequest.seats,
        status: BOOKING_CONFIRMED
    };
    reservationInventory.add(reservation);

    return reservation;
}
