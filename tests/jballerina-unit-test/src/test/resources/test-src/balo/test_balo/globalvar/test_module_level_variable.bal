import testorg/foo;

function getGlobalVars() returns stream<int> {
    return foo:getStreamOfInt();
}
