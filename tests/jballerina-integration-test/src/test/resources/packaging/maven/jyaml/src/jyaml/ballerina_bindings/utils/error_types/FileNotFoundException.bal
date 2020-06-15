// Ballerina error type for java.io.FileNotFoundException.

const FILENOTFOUNDEXCEPTION = "FileNotFoundException";

type FileNotFoundExceptionData record {
    string message;
    error cause?;
};

type FileNotFoundException error<FILENOTFOUNDEXCEPTION, FileNotFoundExceptionData>;

