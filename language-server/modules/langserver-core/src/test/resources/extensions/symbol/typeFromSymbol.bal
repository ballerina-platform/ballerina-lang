import ballerina/http;

type Input record {
    MessageProperties MessageProperties;
    record {
        int Type;
        string Id;
        boolean Confirmed;
    }[] Assets;
    record {
        string StopId;
        string CurrOrPickedTrailer;
        any Driver2;
        string Driver1;
        decimal Latitude;
        any Unit;
        decimal Longitude;
        string TripId;
        string Weight;
        string BillOfLading;
        string ETA;
        decimal user_id;
        string DroppedTrailer;
        string Seal;
        record {
            anydata[] warnings;
            anydata[] errors;
        } form_meta;
        string Pieces;
    } Content;
    string FormId;
    string CreateDate;
};

type Input2 record {
    http:Bucket bucket;
};

type Output record {
    record {
        string MessageContentType;
        record {
            string[] Content;
            record {
                int OType;
                string OId;
                boolean OConfirmed;
            }[] Assets;
            record {
                decimal Latitude;
                decimal Longitude;
            } Coordinates;
            string FormId;
            string CreateDate;
        } MessageContent;
        string ParentMessageGuid;
        string MessageGuid;
    } data;
    string 'type;
};

type MessageProperties record {
    string EventType;
    string AuditId;
    string MessageId;
};

function transform(Input input, Input2 Input2) returns Output => let string oType = item.Type in {
    data: {
        MessageContent: {
            Assets: from var item in input.Assets
                select {
                    OType: oType,
                    OId: item.Id,
                    OConfirmed: item.Confirmed
                }
        },
        MessageGuid: input.MessageProperties.AuditId,
        MessageContentType: input.MessageProperties.EventType
    }
};
