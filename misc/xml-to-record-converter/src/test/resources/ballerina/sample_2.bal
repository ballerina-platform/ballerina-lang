@xmldata:Name {
    value: "guest"
}
type Guest record {
    string firstName;
    string lastName;
    string email;
    string phone;
};

@xmldata:Name {
    value: "reservationDetails"
}
type ReservationDetails record {
    string location;
    string hotelName;
    string arrivalDate;
    string departureDate;
    int roomCount;
    string roomType;
    string specialRequest;
};

@xmldata:Name {
    value: "reservation"
}
type Reservation record {
    Guest guest;
    ReservationDetails reservationDetails;
};


@xmldata:Name {
    value: "reservations"
}
type Reservations record {
    Reservation reservation;
};
