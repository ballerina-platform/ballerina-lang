type NewRecord record {
    string resourceType;
    string id;
    record {string status; string div;} text;
    record {string system; string value;}[] identifier;
    string status;
    record {record {string system; string code;}[] coding;} 'type;
    string use;
    record {string reference;} patient;
    string created;
    record {string reference;} insurer;
    record {string reference;} provider;
    record {record {string code; string system?;}[] coding;} priority;
    record {record {record {string code; string system?;}[] coding;} 'type;} payee;
    record {int sequence; record {string reference;} provider;}[] careTeam;
    record {int sequence; record {record {string code; string system?;}[] coding;} diagnosisCodeableConcept;}[] diagnosis;
    record {int sequence; boolean focal; record {string system; string value;} identifier; record {string reference;} coverage;}[] insurance;
    record {int sequence; int[] careTeamSequence; record {record {string code; string system?;}[] coding;} productOrService; string servicedDate; record {decimal value; string currency;} unitPrice; record {decimal value; string currency;} net;}[] item;
};
