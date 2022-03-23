type Coverage record {
    string reference;
};

type Identifier record {
    string system;
    string value;
};

type InsuranceItem record {
    Coverage coverage;
    int sequence;
    Identifier identifier;
    boolean focal;
};

type IdentifierItem record {
    string system;
    string value;
};

type UnitPrice record {
    string currency;
    decimal value;
};

type CodingItem record {
    string code;
};

type ProductOrService record {
    CodingItem[] coding;
};

type Net record {
    string currency;
    decimal value;
};

type ItemItem record {
    UnitPrice unitPrice;
    int sequence;
    int[] careTeamSequence;
    ProductOrService productOrService;
    string servicedDate;
    Net net;
};

type DiagnosisCodeableConcept record {
    CodingItem[] coding;
};

type DiagnosisItem record {
    int sequence;
    DiagnosisCodeableConcept diagnosisCodeableConcept;
};

type Type record {
    CodingItem[] coding;
};

type Priority record {
    CodingItem[] coding;
};

type Payee record {
    Type 'type;
};

type Provider record {
    string reference;
};

type Patient record {
    string reference;
};

type Insurer record {
    string reference;
};

type Text record {
    string div;
    string status;
};

type CareTeamItem record {
    int sequence;
    Provider provider;
};

type NewRecord record {
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
};
