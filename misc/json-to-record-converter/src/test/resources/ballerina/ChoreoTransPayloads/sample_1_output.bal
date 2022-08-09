type Position record {
    decimal Latitude;
    decimal Longitude;
};

type AssetsItem record {
    int Type;
    string Id;
    boolean Confirmed;
};

type TripInformation record {
    string TripName;
    decimal Move;
    decimal Leg;
    decimal Stop;
    decimal OrderHeader;
    AssetsItem[] Assets;
    anydata[] AdditionalDataElements;
};

type HeaderInformation record {
    string CreateDateUtc;
};

type MessageContent record {
    anydata[] Assets;
    string StatusDate;
    int Speed;
    int Heading;
    Position Position;
    string Description;
    string IgnitionStatus;
    int Odometer;
    anydata Zip?;
    anydata City?;
    anydata State?;
    anydata Distance?;
    TripInformation TripInformation;
    HeaderInformation HeaderInformation;
};

type Data record {
    string MessageGuid;
    string ParentMessageGuid;
    string MessageContentType;
    MessageContent MessageContent;
};

type DtosItem record {
    string 'type;
    Data data;
};

type NewRecord record {
    DtosItem[] dtos;
};
