// Ballerina error type for java.lang.InterruptedException.

const INTERRUPTEDEXCEPTION = "InterruptedException";

type InterruptedExceptionData record {
    string message;
    error cause?;
};

type InterruptedException error<INTERRUPTEDEXCEPTION, InterruptedExceptionData>;

