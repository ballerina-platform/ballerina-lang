type AssetsItem record {
    int Type;
    string Id;
    boolean Confirmed;
};

type TripInformation record {
    anydata TripName;
    anydata Move;
    anydata Leg;
    anydata Stop;
    int OrderHeader;
    AssetsItem[] Assets;
    anydata[] AdditionalDataElements;
};

type HeaderInformation record {
    string CreateDateUtc;
};

type Content record {
    int Odometer;
    anydata UnitTrips;
    anydata Position;
    TripInformation TripInformation;
    HeaderInformation HeaderInformation;
};

type MessageProperties record {
    string EventType;
    string AuditId;
    string MessageId;
};

type NewRecord record {
    AssetsItem[] Assets;
    string CreateDate;
    string ContentType;
    Content Content;
    MessageProperties MessageProperties;
};
