type AssetsItem record {|
    int Type;
    string Id;
    boolean Confirmed?;
    json...;
|};

type TripInformation record {|
    json TripName;
    json Move;
    int Leg;
    json Stop;
    int OrderHeader;
    AssetsItem[] Assets;
    json AdditionalDataElements;
    json...;
|};

type HeaderInformation record {|
    string CreateDateUtc;
    json...;
|};

type Content record {|
    int Response;
    boolean RedispatchNow;
    string Reason;
    json AdditionalDataElements;
    TripInformation TripInformation;
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
    string ContentType;
    string CreateDate;
    Content Content;
    MessageProperties MessageProperties;
    json...;
|};
