// Ballerina error type for FULL_CLASS_NAME.

const SHORT_CLASS_NAME_CAPS = "SHORT_CLASS_NAME";

type SHORT_CLASS_NAMEData record {
    string message;
};

type SHORT_CLASS_NAME distinct error<SHORT_CLASS_NAMEData>;
