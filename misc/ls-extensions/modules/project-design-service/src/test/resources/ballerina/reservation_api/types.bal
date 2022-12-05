type Flight record {
    readonly string flightNumber;
    readonly string flightDate;
    int available;
    int totalCapacity;
};

public type Reservation record {|
    string flightNumber;
    string origin;
    string destination;
    string flightDate;
    int seats;
    int...;
|};


type ConfirmedReservation record {
    readonly int id;
    string flightNumber;
    string origin;
    string destination;
    string flightDate;
    string bookingDate;
    float fare;
    int seats;
    BookingStatus status;
};

enum BookingStatus {
    NEW,
    BOOKING_CONFIRMED,
    CHECKED_IN
}
