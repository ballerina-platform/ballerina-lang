type Text record {
    string div;
    string status;
};

type IdentifierItem record {
    string system;
    string value;
};

type Patient record {
    string reference;
};

type Insurer record {
    string reference;
};

type Priority record {
    CodingItem[] coding;
};

type Prescription record {
    string reference;
};

type Type record {
    CodingItem[] coding;
};

type Payee record {
    Type 'type;
};

type Provider record {
    string reference;
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

type CodingItem record {
    string code;
};

type ProductOrService record {
    CodingItem[] coding;
};

type UnitPrice record {
    string currency;
    decimal value;
};

type Net record {
    string currency;
    decimal value;
};

type DetailItem record {
    int sequence;
    ProductOrService productOrService;
    UnitPrice unitPrice;
    Net net;
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
