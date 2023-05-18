type AssetsItem record {
    int Type;
    string Id;
};

type RecipientsItem record {
    string UserId;
    string Name;
    anydata EmailAddress;
};

type HeaderInformation record {
    string CreateDateUtc;
};

type Content record {
    string Message;
    RecipientsItem[] Recipients;
    anydata TripInformation;
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
