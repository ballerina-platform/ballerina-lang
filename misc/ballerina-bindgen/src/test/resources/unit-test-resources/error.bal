// Ballerina error type for `java.io.IOException`.

const IOEXCEPTION = "IOException";

type IOExceptionData record {
    string message;
};

type IOException distinct error<IOExceptionData>;
