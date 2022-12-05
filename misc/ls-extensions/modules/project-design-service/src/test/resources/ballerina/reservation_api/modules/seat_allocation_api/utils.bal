import ballerina/log;

function addReservation(SeatAllocation seatAllocation) returns SeatAllocation|error {

    Flight? inventory = find(seatAllocation.flightNumber, seatAllocation.flightDate);
    if inventory is () {
        return error(string `inventory not found for flight number: ${seatAllocation.flightNumber}, flight date : ${seatAllocation.flightDate}`);
    }

    if seatAllocation.seats > inventory.available {
        return error(string `not enought seats for flight number: ${seatAllocation.flightNumber}, flight date : ${seatAllocation.flightDate}`);
    }

    inventory.available = inventory.available - seatAllocation.seats;
    return seatAllocation;
}

function find(string flightNumber, string flightDate) returns Flight? {
    log:printInfo("Flight Date : " + flightDate);
    return flightInventory[flightNumber, flightDate];
}

function findByFlightNumber(string flightNumber) returns Flight[] {
    Flight[] flights = from var f in flightInventory
        where f.flightNumber == flightNumber
        order by f.flightDate
        select f;
    return flights;
}
