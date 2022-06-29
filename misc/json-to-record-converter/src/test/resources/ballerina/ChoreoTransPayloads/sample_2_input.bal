type AssetsItem record {
    int Type;
    string Id;
    boolean Confirmed?;
};

type TripInformation record {
    any TripName?;
    any Move?;
    int Leg;
    any Stop?;
    int OrderHeader;
    AssetsItem[] Assets;
    any AdditionalDataElements?;
};

type HeaderInformation record {
    string CreateDateUtc;
};

type Content record {
    int Response;
    boolean RedispatchNow;
    string Reason;
    any AdditionalDataElements?;
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
