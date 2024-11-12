type AssetsItem record {|
    int Type;
    string Id;
    json...;
|};

type RecipientsItem record {|
    string UserId;
    string Name;
    json EmailAddress;
    json...;
|};

type HeaderInformation record {|
    string CreateDateUtc;
    json...;
|};

type Content record {|
    string Message;
    RecipientsItem[] Recipients;
    json TripInformation;
    HeaderInformation HeaderInformation;
    json...;
|};

type MessageProperties record {|
    string EventType;
    string AuditId;
    string MessageId;
    json...;
|};

type NewRecord record {|
    AssetsItem[] Assets;
    string CreateDate;
    string ContentType;
    Content Content;
    MessageProperties MessageProperties;
    json...;
|};
