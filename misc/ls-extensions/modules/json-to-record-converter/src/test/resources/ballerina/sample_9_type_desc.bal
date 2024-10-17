type Person record {|
    record {|

        record {|
            string reference;
            json...;
        |} coverage;

        int sequence;

        record {|
            string system;
            string value;
            json...;
        |} identifier;

        boolean focal;
        json...;
    |}[] insurance;

    record {|
        string system;
        string value;
        json...;
    |}[] identifier;

    record {|

        record {|
            string currency;
            decimal value;
            json...;
        |} unitPrice;

        int sequence;
        int[] careTeamSequence;

        record {|
            record {|
                string code;
                json...;
            |}[] coding;
            json...;
        |} productOrService;

        string servicedDate;
        record {|
            string currency;
            decimal value;
            json...;
        |} net;
        json...;
    |}[] item;

    string use;
    string created;

    record {|
        int sequence;

        record {|
            record {|
                string code;
                json...;
            |}[] coding;
            json...;
        |} diagnosisCodeableConcept;
        json...;
    |}[] diagnosis;

    record {|
        record {|
            string system;
            string code;
            json...;
        |}[] coding;
        json...;
    |} 'type;

    record {|
        record {|
            string code;
            json...;
        |}[] coding;
        json...;
    |} priority;

    record {|
        record {|
            record {|
                string code;
                json...;
            |}[] coding;
            json...;
        |} 'type;
        json...;
    |} payee;

    record {|
        string reference;
        json...;
    |} provider;

    record {|
        string reference;
        json...;
    |} patient;

    record {|
        string reference;
        json...;
    |} insurer;

    string id;

    record {|
        string div;
        string status;
        json...;
    |} text;

    record {|
        int sequence;

        record {|
            string reference;
            json...;
        |} provider;
        json...;
    |}[] careTeam;

    string resourceType;
    string status;
    json...;
|};
