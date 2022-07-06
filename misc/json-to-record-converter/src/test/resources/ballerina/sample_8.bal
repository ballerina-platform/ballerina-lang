type Text record {
    string status;
    string div;
};

type IdentifierItem record {
    string system;
    string value;
};

type CodingItem record {
    string code;
    string system?;
};

type Type record {
    CodingItem[] coding;
};

type Patient record {
    string reference;
};

type Insurer record {
    string reference;
};

type Provider record {
    string reference;
};

type Priority record {
    CodingItem[] coding;
};

type Prescription record {
    string reference;
};

type Payee record {
    Type 'type;
};

type CareTeamItem record {
    int sequence;
    Provider provider;
};

type DiagnosisCodeableConcept record {
    CodingItem[] coding;
};

type DiagnosisItem record {
    int sequence;
    DiagnosisCodeableConcept diagnosisCodeableConcept;
};

type Coverage record {
    string reference;
};

type InsuranceItem record {
    int sequence;
    boolean focal;
    Coverage coverage;
};

type ProductOrService record {
    CodingItem[] coding;
};

type UnitPrice record {
    decimal value;
    string currency;
};

type Net record {
    decimal value;
    string currency;
};

type DetailItem record {
    int sequence;
    ProductOrService productOrService;
    UnitPrice unitPrice;
    Net net;
    Quantity quantity?;
    decimal factor?;
};

type Quantity record {
    int value;
};

type ItemItem record {
    int sequence;
    int[] careTeamSequence;
    ProductOrService productOrService;
    string servicedDate;
    UnitPrice unitPrice;
    Net net;
    DetailItem[] detail;
};

type NewRecord record {
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
};
