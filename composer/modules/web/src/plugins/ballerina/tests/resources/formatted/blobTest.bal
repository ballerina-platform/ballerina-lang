import ballerina/lang.blobs;

function toString (blob content, string charset) (string) {
    return blobs:toString(content, charset);
}