type NewRecord record {|
    string resourceType;
    string id;
    record {|
        string status;
        string div;
        json...;
    |} text;
    record {|
        string system;
        string value;
        json...;
    |}[] identifier;
    string status;
    record {|
        record {|
            string code;
            string system?;
            json...;
        |}[] coding;
        json...;
    |} 'type;
    string use;
    record {|
        string reference;
        json...;
    |} patient;
    string created;
    record {|
        string reference;
        json...;
    |} insurer;
    record {|
        string reference;
        json...;
    |} provider;
    record {|
        record {|
            string code;
            string system?;
            json...;
        |}[] coding;
        json...;
    |} priority;
    record {|
        string reference;
        json...;
    |} prescription;
    record {|
        record {|
            record {|
                string code;
                string system?;
                json...;
            |}[] coding;
            json...;
        |} 'type;
        json...;
    |} payee;
    record {|
        int sequence;
        record {|
            string reference;
            json...;
        |} provider;
        json...;
    |}[] careTeam;
    record {|
        int sequence;
        record {|
            record {|
                string code;
                string system?;
                json...;
            |}[] coding;
            json...;
        |} diagnosisCodeableConcept;
        json...;
    |}[] diagnosis;
    record {|
        int sequence;
        boolean focal;
        record {|
            string reference;
            json...;
        |} coverage;
        json...;
    |}[] insurance;
    record {|
        int sequence;
        int[] careTeamSequence;
        record {|
            record {|
                string code;
                string system?;
                json...;
            |}[] coding;
            json...;
        |} productOrService;
        string servicedDate;
        record {|
            decimal value;
            string currency;
            json...;
        |} unitPrice;
        record {|
            decimal value;
            string currency;
            json...;
        |} net;
        record {|
            int sequence;
            record {|
                record {|
                    string code;
                    string system?;
                    json...;
                |}[] coding;
                json...;
            |} productOrService;
            record {|
                decimal value;
                string currency;
                json...;
            |} unitPrice;
            record {|
                decimal value;
                string currency;
                json...;
            |} net;
            record {|
                int value;
                json...;
            |} quantity?;
            decimal factor?;
            json...;
        |}[] detail;
        json...;
    |}[] item;
    json...;
|};
