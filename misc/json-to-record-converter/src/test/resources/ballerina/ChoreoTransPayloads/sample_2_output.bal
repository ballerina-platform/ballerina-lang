type AssetsItem record {|
    int Type;
    string Id;
    boolean Confirmed;
    json...;
|};

type TripInformation record {|
    string TripName;
    decimal Move;
    int Leg;
    decimal Stop;
    int OrderHeader;
    AssetsItem[] Assets;
    json[] AdditionalDataElements;
    json...;
|};

type HeaderInformation record {|
    string CreateDateUtc;
    json...;
|};

type MessageContent record {|
    int Response;
    string RedispatchNow;
    string Reason;
    json[] AdditionalDataElements;
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
