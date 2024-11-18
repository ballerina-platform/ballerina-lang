type Position record {|
    decimal Latitude;
    decimal Longitude;
    json...;
|};

type AssetsItem record {|
    int Type;
    string Id;
    boolean Confirmed;
    json...;
|};

type TripInformation record {|
    string TripName;
    decimal Move;
    decimal Leg;
    decimal Stop;
    decimal OrderHeader;
    AssetsItem[] Assets;
    json[] AdditionalDataElements;
    json...;
|};

type HeaderInformation record {|
    string CreateDateUtc;
    json...;
|};

type MessageContent record {|
    json[] Assets;
    string StatusDate;
    int Speed;
    int Heading;
    Position Position;
    string Description;
    string IgnitionStatus;
    int Odometer;
    json Zip;
    json City;
    json State;
    json Distance;
    TripInformation TripInformation;
    HeaderInformation HeaderInformation;
    json...;
|};

type Data record {|
    string MessageGuid;
    string ParentMessageGuid;
    string MessageContentType;
    MessageContent MessageContent;
    json...;
|};

type DtosItem record {|
    string 'type;
    Data data;
    json...;
|};

type NewRecord record {|
    DtosItem[] dtos;
    json...;
|};
