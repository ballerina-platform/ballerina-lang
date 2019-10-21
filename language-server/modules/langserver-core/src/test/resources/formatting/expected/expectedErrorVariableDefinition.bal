type SMS error<string, record {|string message?; error cause?; string...;|}>;
type SMA error<string, record {|string message?; error cause?; anydata...;|}>;
type CMS error<string, record {|string message?; error cause?; string...;|}>;
type CMA error<string, record {|string message?; error cause?; anydata...;|}>;
const ERROR1 = "Some Error One";
const ERROR2 = "Some Error Two";

function testBasicErrorVariableWithMapDetails() returns [string, string, string, string, map<string>, string?, string?,
string?, map<any>, any, any, any] {
    SMS err1 = error("Error One",
    message = "Msg One",
    detail = "Detail Msg"
    );
    SMA err2 = error("Error Two",
    message = "Msg Two",
    fatal = true
    );
    SMS error (reason11, ...detail11) = err1;
    SMS error (reason12, message = message12, detail = detail12, extra = extra12) = err1;

    SMA
    error
    (
    reason21
    ,
    ...detail21
    )
    =
    err2
    ;
    SMA error (reason22, message = message22, detail = detail22,
    extra = extra22) = err2;

    return [reason11, reason12, reason21, reason22, detail11, message12, detail12, extra12, detail21, message22,
    detail22, extra22];
}

function testErrorBindingPattern() returns string {
    error<string, record {|string message?; error cause?; string...;|}> err1 = error("Error Code", message = "Fatal");
    [string, map<any>] | error t1 = err1;
    match t1 {
        var [reason, detail] => {
            return "Matched with tuple : " + reason + " " + "io:sprintf(\"%s\", detail)";
        }
        error (var reason, message = message) => {
            return "Matched with error : " + reason + " {\"message\":\"" + <string>message + "\"}";
        }
    }

    return "";
}

function testErrorBindingPattern2() returns string {
    error<string, record {|string message?; error cause?; string...;|}> err1 = error("Error Code", message = "Msg");
    [string, map<any>] | error t1 = err1;
    match t1 {
        var [reason, detail] => {
            return "Matched with tuple : " + reason + " " + "io:sprintf(\"%s\", detail)";
        }
        var error (reason, message = message) => {
            return "Matched with error : " + reason + " {\"message\":\"" + <string>message + "\"}";
        }
    }

    return "";
}

function testErrorBindingPattern3() returns string {
    error<string, record {|string message?; error cause?; string...;|}> err1 = error("Error Code", message = "Msg");
    [string, map<any>] | error t1 = err1;
    match t1 {
        var [reason, detail] => {
            return "Matched with tuple : " + reason + " " + "io:sprintf(\"%s\", detail)";
        }
        error (var reason, message = message, ...var details1) => {
            return "Matched with error : " + reason + " {\"message\":\"" + <string>message + "\"}";
        }
    }

    return "";
}

function testErrorBindingPattern4() returns string {
    error<string, record {|string message?; error cause?; string...;|}> err1 = error("Error Code", message = "Msg");
    [string, map<any>] | error t1 = err1;
    match t1 {
        var [reason, detail] => {
            return "Matched with tuple : " + reason + " " + "io:sprintf(\"%s\", detail)";
        }
        error
        (
        var
        reason
        ,
        message
        =
        message
        ) => {
            return "Matched with error : " + reason + " {\"message\":\"" + <string>message + "\"}";
        }
    }

    return "";
}

function testErrorBindingPattern5() returns string {
    error<string, record {|string message?; error cause?; string...;|}> err1 = error("Error Code", message = "Msg");
    [string, map<any>] | error t1 = err1;
    match t1 {
        var [reason, detail] => {
            return "Matched with tuple : " + reason + " " + "io:sprintf(\"%s\", detail)";
        }
        var
        error (
        reason
        ,
        message
        =
        message
        ) => {
            return "Matched with error : " + reason + " {\"message\":\"" + <string>message + "\"}";
        }
    }

    return "";
}

function testErrorBindingPattern6() returns string {
    error<string, record {|string message?; error cause?; string...;|}> err1 = error("Error Code", message = "Msg");
    [string, map<any>] | error t1 = err1;
    match t1 {
        var [reason, detail] => {
            return "Matched with tuple : " + reason + " " + "io:sprintf(\"%s\", detail)";
        }
        error
        (
        var
        reason
        ,
        message
        =
        message,
        ...
        var
        details1
        ) => {
            return "Matched with error : " + reason + " {\"message\":\"" + <string>message + "\"}";
        }
    }

    return "";
}

function testIndirectErrorVariableDef1() {
    SMS e = error("the reason", message = "msg");
    var SMS (message = message, other = other, ...rest) = e;
}

function testIndirectErrorVariableDef2() {
    SMS e = error("the reason", message = "msg");
    var
    SMS
    (
    message = message
    ,
    other = other
    ,
    ...rest
    )
    =
    e
    ;
}

type ErrorData record {
    string message?;
    error cause?;
};

type ER error<string, ErrorData>;

function testIndirectErrorMatchPattern1() returns string {
    ER err1 = error("Error Code", message = "Msg");
    match err1 {
        ER (message = m, ...var rest) => {
            return <string>m;
        }
    }
    return "Default";
}

function testIndirectErrorMatchPattern2() returns string {
    ER err1 = error("Error Code", message = "Msg");
    match err1 {
        ER
        (
        message
        =
        m
        ,
        ...
        var
        rest
        )
        =>
        {
            return <string>m;
        }
    }
    return "Default";
}
