type Text record {|
    string status;
    string div;
    json...;
|};

type IdentifierItem record {|
    string system;
    string value;
    json...;
|};

type CodingItem record {|
    string code;
    string system?;
    json...;
|};

type Type record {|
    CodingItem[] coding;
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

type Provider record {|
    string reference;
    json...;
|};

type Priority record {|
    CodingItem[] coding;
    json...;
|};

type Prescription record {|
    string reference;
    json...;
|};

type Payee record {|
    Type 'type;
    json...;
|};

type CareTeamItem record {|
    int sequence;
    Provider provider;
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

type Coverage record {|
    string reference;
    json...;
|};

type InsuranceItem record {|
    int sequence;
    boolean focal;
    Coverage coverage;
    json...;
|};

type ProductOrService record {|
    CodingItem[] coding;
    json...;
|};

type UnitPrice record {|
    decimal value;
    string currency;
    json...;
|};

type Net record {|
    decimal value;
    string currency;
    json...;
|};

type Quantity record {|
    int value;
    json...;
|};

type DetailItem record {|
    int sequence;
    ProductOrService productOrService;
    UnitPrice unitPrice;
    Net net;
    Quantity quantity?;
    decimal factor?;
    json...;
|};

type ItemItem record {|
    int sequence;
    int[] careTeamSequence;
    ProductOrService productOrService;
    string servicedDate;
    UnitPrice unitPrice;
    Net net;
    DetailItem[] detail;
    json...;
|};

type NewRecord record {|
    string resourceType;
    string id;
    Text text;
    IdentifierItem[] identifier;
    string status;
    Type 'type;
    string use;
    Patient patient;
    string created;
    Insurer insurer;
    Provider provider;
    Priority priority;
    Prescription prescription;
    Payee payee;
    CareTeamItem[] careTeam;
    DiagnosisItem[] diagnosis;
    InsuranceItem[] insurance;
    ItemItem[] item;
    json...;
|};
