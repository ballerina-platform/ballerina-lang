import ballerina/jballerina.java;

// Returns the MD5 hex digest of a string.
public function md5Hex(string input) returns string {
    handle jInput = java:fromString(input);
    handle result = digestUtilsMd5Hex(jInput);
    return java:toString(result) ?: "";
}

// Returns the SHA-256 hex digest of a string.
public function sha256Hex(string input) returns string {
    handle jInput = java:fromString(input);
    handle result = digestUtilsSha256Hex(jInput);
    return java:toString(result) ?: "";
}

// Returns the SHA-512 hex digest of a string.
public function sha512Hex(string input) returns string {
    handle jInput = java:fromString(input);
    handle result = digestUtilsSha512Hex(jInput);
    return java:toString(result) ?: "";
}

// Internal Java bridge functions (commons-codec DigestUtils)

function digestUtilsMd5Hex(handle data) returns handle = @java:Method {
    'class: "org.apache.commons.codec.digest.DigestUtils",
    name: "md5Hex",
    paramTypes: ["java.lang.String"]
} external;

function digestUtilsSha256Hex(handle data) returns handle = @java:Method {
    'class: "org.apache.commons.codec.digest.DigestUtils",
    name: "sha256Hex",
    paramTypes: ["java.lang.String"]
} external;

function digestUtilsSha512Hex(handle data) returns handle = @java:Method {
    'class: "org.apache.commons.codec.digest.DigestUtils",
    name: "sha512Hex",
    paramTypes: ["java.lang.String"]
} external;

function getSystemOut() returns handle = @java:FieldGet {
    'class: "java.lang.System",
    name: "out"
} external;

function printlnOnPrintStream(handle receiver, handle data) = @java:Method {
    'class: "java.io.PrintStream",
    name: "println",
    paramTypes: ["java.lang.String"]
} external;

// Public function to print using System.out.println
public function systemOutPrintln(string message) {
    handle out = getSystemOut();
    handle jMessage = java:fromString(message);
    printlnOnPrintStream(out, jMessage);
}
