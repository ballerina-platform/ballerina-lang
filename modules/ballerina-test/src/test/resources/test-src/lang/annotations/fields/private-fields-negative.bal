annotation Test {
    string msg;
    private string pmsg;
    int ival;
    private int ipval;
}

@Test {msg:"abc", pmsg:"pmsg"}
function main (string[] args) {
    print("hello...! I am a negative test case.");
}
