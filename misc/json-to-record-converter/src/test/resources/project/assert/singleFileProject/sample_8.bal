type Text_01 record {
    string status;
    string div;
};

type Identifier_01Item record {
    string system;
    string value;
};

type Coding_01Item record {
    string code;
    string system?;
};

type Type_01 record {
    Coding_01Item[] coding;
};

type Patient_01 record {
    string reference;
};

type Insurer_01 record {
    string reference;
};

type Provider_01 record {
    string reference;
};

type Priority_01 record {
    Coding_01Item[] coding;
};

type Prescription_01 record {
    string reference;
};

type Payee_01 record {
    Type_01 'type;
};

type CareTeam_01Item record {
    int sequence;
    Provider_01 provider;
};

type DiagnosisCodeableConcept_01 record {
    Coding_01Item[] coding;
};

type Diagnosis_01Item record {
    int sequence;
    DiagnosisCodeableConcept_01 diagnosisCodeableConcept;
};

type Coverage_01 record {
    string reference;
};

type Insurance_01Item record {
    int sequence;
    boolean focal;
    Coverage_01 coverage;
};

type ProductOrService_01 record {
    Coding_01Item[] coding;
};

type UnitPrice_01 record {
    decimal value;
    string currency;
};

type Net_01 record {
    decimal value;
    string currency;
};

type Quantity_01 record {
    int value;
};

type Detail_01Item record {
    int sequence;
    ProductOrService_01 productOrService;
    UnitPrice_01 unitPrice;
    Net_01 net;
    Quantity_01 quantity?;
    decimal factor?;
};

type Item_01Item record {
    int sequence;
    int[] careTeamSequence;
    ProductOrService_01 productOrService;
    string servicedDate;
    UnitPrice_01 unitPrice;
    Net_01 net;
    Detail_01Item[] detail;
};

type NewRecord_01 record {
    string resourceType;
    string id;
    Text_01 text;
    Identifier_01Item[] identifier;
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
    CareTeam_01Item[] careTeam;
    Diagnosis_01Item[] diagnosis;
    Insurance_01Item[] insurance;
    Item_01Item[] item;
};
