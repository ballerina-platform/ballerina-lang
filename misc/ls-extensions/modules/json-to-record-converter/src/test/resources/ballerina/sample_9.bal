type Coverage record {|
    string reference;
    json...;
|};

type Identifier record {|
    string system;
    string value;
    json...;
|};

type InsuranceItem record {|
    Coverage coverage;
    int sequence;
    Identifier identifier;
    boolean focal;
    json...;
|};

type IdentifierItem record {|
    string system;
    string value;
    json...;
|};

type UnitPrice record {|
    string currency;
    decimal value;
    json...;
|};

type CodingItem record {|
    string code;
    json...;
|};

type ProductOrService record {|
    CodingItem[] coding;
    json...;
|};

type Net record {|
    string currency;
    decimal value;
    json...;
|};

type ItemItem record {|
    UnitPrice unitPrice;
    int sequence;
    int[] careTeamSequence;
    ProductOrService productOrService;
    string servicedDate;
    Net net;
    json...;
|};

type DiagnosisCodeableConcept record {|
    CodingItem[] coding;
    json...;
|};

type DiagnosisItem record {|
    int sequence;
    DiagnosisCodeableConcept diagnosisCodeableConcept;
    json...;
|};

type Type record {|
    CodingItem[] coding;
    json...;
|};

type Priority record {|
    CodingItem[] coding;
    json...;
|};

type Payee record {|
    Type 'type;
    json...;
|};

type Provider record {|
    string reference;
    json...;
|};

type Patient record {|
    string reference;
    json...;
|};

type Insurer record {|
    string reference;
    json...;
|};

type Text record {|
    string div;
    string status;
    json...;
|};

type CareTeamItem record {|
    int sequence;
    Provider provider;
    json...;
|};

type NewRecord record {|
    InsuranceItem[] insurance;
    IdentifierItem[] identifier;
    ItemItem[] item;
    string use;
    string created;
    DiagnosisItem[] diagnosis;
    Type 'type;
    Priority priority;
    Payee payee;
    Provider provider;
    Patient patient;
    Insurer insurer;
    string id;
    Text text;
    CareTeamItem[] careTeam;
    string resourceType;
    string status;
    json...;
|};
