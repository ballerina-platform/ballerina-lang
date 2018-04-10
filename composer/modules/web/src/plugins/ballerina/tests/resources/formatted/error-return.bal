struct InvalidNameError {
    string msg;
    error? cause;
    stackFrame[] stackTrace;
    string companyName;
}

function getQuote (string name) (float, InvalidNameError) {

    if (name == "FOO") {
        return 10.5, null;
    } else if (name == "BAR") {
        return 11.5, null;
    }

    InvalidNameError err = {msg:"invalid name", companyName:name};
    return -1.0, err;
}

function testReturnError () (string, string, string, string) {

    string a;
    string b;
    string c;
    string d;

    float quoteValue;
    // Special identifier "_" will be used to ignore values.

    quoteValue, _ = getQuote("FOO");
    a = "FOO:" + quoteValue;

    // Ignore error.
    quoteValue, _ = getQuote("QUX");
    b = "QUX:" + quoteValue;

    // testing for errors.
    InvalidNameError errorBAZ;
    quoteValue, errorBAZ = getQuote("BAZ");

    if (errorBAZ != null) {
        // error occurred. Recover from the error by assigning 0.
        quoteValue = 0.0;
    }
    c = "BAZ:" + quoteValue;

    InvalidNameError errorBAR;
    quoteValue, errorBAZ = getQuote("BAR");
    if (errorBAZ != null) {
        // error occurred. Recover from the error by assigning 0.
        quoteValue = 0.0;
    }
    d = "BAR:" + quoteValue;

    return a, b, c, d;
}

function testReturnAndThrowError () (string) {
    try {
        checkAndThrow();
    } catch (error e) {
        return e.msg;
    }
    return "OK";
}

function checkAndThrow () {
    InvalidNameError err;
    float quoteValue;
    quoteValue, err = getQuote("BAZ");
    if (err != null) {
        throw err;
    }
}