type AssetsItem record {
    int Type;
    string Id;
    boolean Confirmed;
};

type TripInformation record {
    string TripName;
    decimal Move;
    int Leg;
    decimal Stop;
    int OrderHeader;
    AssetsItem[] Assets;
    any[] AdditionalDataElements;
};

type HeaderInformation record {
    string CreateDateUtc;
};

type MessageContent record {
    int Response;
    string RedispatchNow;
    string Reason;
    any[] AdditionalDataElements;
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
