public type Flight record {
    readonly string flightNumber;
    readonly string flightDate;
    int available;
    int totalCapacity;
};

public type SeatAllocation record {|
    string flightNumber;
    string flightDate;
    int seats;
|};