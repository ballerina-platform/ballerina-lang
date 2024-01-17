type RecipientsItem record {
    string UserId;
    string Name;
    string EmailAddress;
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
    string Message;
    RecipientsItem[] Recipients;
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
