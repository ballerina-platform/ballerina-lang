// Ballerina error type for java.io.IOException.

const IOEXCEPTION = "IOException";

type IOExceptionData record {
    string message;
    error cause?;
};

type IOException error<IOEXCEPTION, IOExceptionData>;

