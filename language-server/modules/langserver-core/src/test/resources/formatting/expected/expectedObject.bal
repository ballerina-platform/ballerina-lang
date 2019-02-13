import ballerina/io;

type ObjectName1 object {

};

type ObjectName2 object {
    private int bd = 0;
    public int a = 0;
    public string s;
    string h;
};

type ObjectName3 object {
    public int a = 0;
    public string s;
    public ObjectName1? b;
    string h;

    function __init() {
        s = "";
    }

    function testS() {
        a = 1;
    }

    function sd();
};

function close(io:ReadableByteChannel | io:WritableByteChannel ch) {
    abstract object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}
