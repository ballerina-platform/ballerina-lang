// Ballerina error type for `FULL_CLASS_NAME`.

ACCESS_MODIFIERconst SIMPLE_CLASS_NAME_CAPS = "SIMPLE_CLASS_NAME";

type SIMPLE_CLASS_NAMEData record {
    string message;
};

ACCESS_MODIFIERtype SIMPLE_CLASS_NAME distinct error<SIMPLE_CLASS_NAMEData>;
