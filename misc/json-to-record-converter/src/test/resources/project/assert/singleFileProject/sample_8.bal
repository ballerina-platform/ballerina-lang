type Text_01 record {|
    string status;
    string div;
    json...;
|};

type IdentifierItem_01 record {|
    string system;
    string value;
    json...;
|};

type CodingItem_01 record {|
    string code;
    string system?;
    json...;
|};

type Type_01 record {|
    CodingItem_01[] coding;
    json...;
|};

type Patient_01 record {|
    string reference;
    json...;
|};

type Insurer_01 record {|
    string reference;
    json...;
|};

type Provider_01 record {|
    string reference;
    json...;
|};

type Priority_01 record {|
    CodingItem_01[] coding;
    json...;
|};

type Prescription_01 record {|
    string reference;
    json...;
|};

type Payee_01 record {|
    Type_01 'type;
    json...;
|};

type CareTeamItem_01 record {|
    int sequence;
    Provider_01 provider;
    json...;
|};

type DiagnosisCodeableConcept_01 record {|
    CodingItem_01[] coding;
    json...;
|};

type DiagnosisItem_01 record {|
    int sequence;
    DiagnosisCodeableConcept_01 diagnosisCodeableConcept;
    json...;
|};

type Coverage_01 record {|
    string reference;
    json...;
|};

type InsuranceItem_01 record {|
    int sequence;
    boolean focal;
    Coverage_01 coverage;
    json...;
|};

type ProductOrService_01 record {|
    CodingItem_01[] coding;
    json...;
|};

type UnitPrice_01 record {|
    decimal value;
    string currency;
    json...;
|};

type Net_01 record {|
    decimal value;
    string currency;
    json...;
|};

type Quantity_01 record {|
    int value;
    json...;
|};

type DetailItem_01 record {|
    int sequence;
    ProductOrService_01 productOrService;
    UnitPrice_01 unitPrice;
    Net_01 net;
    Quantity_01 quantity?;
    decimal factor?;
    json...;
|};

type ItemItem_01 record {|
    int sequence;
    int[] careTeamSequence;
    ProductOrService_01 productOrService;
    string servicedDate;
    UnitPrice_01 unitPrice;
    Net_01 net;
    DetailItem_01[] detail;
    json...;
|};

type NewRecord_01 record {|
    string resourceType;
    string id;
    Text_01 text;
    IdentifierItem_01[] identifier;
    string status;
    Type_01 'type;
    string use;
    Patient_01 patient;
    string created;
    Insurer_01 insurer;
    Provider_01 provider;
    Priority_01 priority;
    Prescription_01 prescription;
    Payee_01 payee;
    CareTeamItem_01[] careTeam;
    DiagnosisItem_01[] diagnosis;
    InsuranceItem_01[] insurance;
    ItemItem_01[] item;
    json...;
|};
