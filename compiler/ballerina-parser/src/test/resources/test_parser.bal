type Person record {
    distinct record { int i; } value;
    distinct int code;
    distinct readonly & int origCode;
    int | distinct record { int i; } uniVal;
    record { distinct record { int code; } rec; } rec;
};
