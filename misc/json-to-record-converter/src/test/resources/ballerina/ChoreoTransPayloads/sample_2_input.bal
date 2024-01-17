type AssetsItem record {
    int Type;
    string Id;
    boolean Confirmed?;
};

type TripInformation record {
    anydata TripName;
    anydata Move;
    int Leg;
    anydata Stop;
    int OrderHeader;
    AssetsItem[] Assets;
    anydata AdditionalDataElements;
};

type HeaderInformation record {
    string CreateDateUtc;
};

type Content record {
    int Response;
    boolean RedispatchNow;
    string Reason;
    anydata AdditionalDataElements;
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
    string ContentType;
    string CreateDate;
    Content Content;
    MessageProperties MessageProperties;
};
