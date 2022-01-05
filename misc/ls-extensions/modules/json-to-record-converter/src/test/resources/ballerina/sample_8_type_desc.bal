type Person record {
    record {
        record {
            string reference;
        } coverage;
        int sequence;
        boolean focal;
    }[] insurance;

    record {
        string system;
        string value;
    }[] identifier;

    record {
        record {
            string currency;
            decimal value;
        } unitPrice;
        int sequence;
        int[] careTeamSequence;

        record {
            record {
                string system;
                string code;
            }[] coding;
        } productOrService;

        string servicedDate;

        record {
            record {
                string currency;
                decimal value;
            } unitPrice;
            int sequence;

            record {
                record {
                    string system;
                    string code;
                }[] coding;
            } productOrService;

            record {
                string currency;
                decimal value;
            } net;
        }[] detail;

        record {
            string currency;
            decimal value;
        } net;
    }[] item;

    string use;
    string created;

    record {
        int sequence;

        record {
            record {
                string code;
            }[] coding;
        } diagnosisCodeableConcept;
    }[] diagnosis;

    record {
        record {
            string system;
            string code;
        }[] coding;
    } 'type;

    record {
        record {
            string code;
        }[] coding;
    } priority;

    record {
        record {
            record {
                string code;
            }[] coding;
        } 'type;
    } payee;

    record {
        string reference;
    } provider;

    record {
        string reference;
    } prescription;

    record {
        string reference;
    } patient;

    record {
        string reference;
    } insurer;

    string id;

    record {
        string div;
        string status;
    } text;

    record {
        int sequence;
        record {
            string reference;
        } provider;
    }[] careTeam;

    string resourceType;
    string status;
};
