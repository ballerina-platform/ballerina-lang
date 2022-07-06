type AssetsItem record {
    int Type;
    string Id;
};

type Position record {
    decimal Latitude;
    decimal Longitude;
};

type HeaderInformation record {
    string CreateDateUtc;
};

type Content record {
    string StatusDate;
    Position Position;
    int Speed;
    int Heading;
    string IgnitionStatus;
    decimal Odometer;
    string Description;
    anydata AdditionalDataElements?;
    anydata TripInformation?;
    HeaderInformation HeaderInformation;
};

type MessageProperties record {
    string EventType;
    string AuditId;
    string MessageId;
};

type NewRecord record {
    AssetsItem[] Assets;
    string ContentType;
    string CreateDate;
    Content Content;
    MessageProperties MessageProperties;
};
