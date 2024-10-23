type AssetsItem record {|
    int Type;
    string Id;
    json...;
|};

type Position record {|
    decimal Latitude;
    decimal Longitude;
    json...;
|};

type HeaderInformation record {|
    string CreateDateUtc;
    json...;
|};

type Content record {|
    string StatusDate;
    Position Position;
    int Speed;
    int Heading;
    string IgnitionStatus;
    decimal Odometer;
    string Description;
    json AdditionalDataElements;
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
    string ContentType;
    string CreateDate;
    Content Content;
    MessageProperties MessageProperties;
    json...;
|};
