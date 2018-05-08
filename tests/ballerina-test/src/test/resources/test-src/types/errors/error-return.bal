public type InvalidNameError {
    string message;
    error? cause;
    string companyName;
};

function getQuote(string name) returns (float|InvalidNameError) {

    if (name == "FOO") {
        return 10.5;
    } else if (name == "BAR") {
        return 11.5;
    }

    InvalidNameError err = { message: "invalid name", companyName : name };
    return err;
}

function testReturnError() returns (string, string, string, string) {

    string a;
    string b;
    string c;
    string d;

    float quoteValue;
    // Special identifier "=?" will be used to ignore values.

    quoteValue =check getQuote("FOO");
    a = "FOO:" + quoteValue;

    // Ignore error.
    var r = getQuote("QUX");
    match r {
        float qVal => b = "QUX:" + qVal;
        InvalidNameError err => b = "QUX:ERROR";
    }

    // testing for errors.
    // error occurred. Recover from the error by assigning 0.
    var q = getQuote("BAZ");
    match q {
        float qVal => c = "BAZ:" + quoteValue;
        InvalidNameError errorBAZ => {
            quoteValue = 0.0;
            c = "BAZ:" + quoteValue;
        }
    }

    var p = getQuote("BAR");

    match p {
        float qVal => d = "BAR:" + qVal;
        InvalidNameError errorBAR => {
            quoteValue = 0.0;
            d = "BAR:ERROR";
        }
    }
    return (a,b,c,d);
}

function testReturnAndThrowError() returns (string){
    try{
        checkAndThrow();
    }catch(error e){
        error c;
        match e.cause { 
            error s => c = s;
            () => c = {}; 
        }
        return c.message;
    }
    return "OK";
}

function checkAndThrow(){
    float qVal;
    var p = getQuote("BAZ");

    match p {
        float quoteValue => qVal = quoteValue;
        InvalidNameError err => throw err;
    }
}
