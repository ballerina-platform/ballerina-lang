type AssetsItem record {
    int Type;
    string Id;
    boolean Confirmed;
};

type TripInformation record {
    any TripName?;
    any Move?;
    any Leg?;
    any Stop?;
    int OrderHeader;
    AssetsItem[] Assets;
    any[] AdditionalDataElements;
};

type HeaderInformation record {
    string CreateDateUtc;
};

type Content record {
    int Odometer;
    any UnitTrips?;
    any Position?;
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
