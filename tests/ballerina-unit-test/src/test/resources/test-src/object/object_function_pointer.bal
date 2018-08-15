
public type Window object {
    public string v = "hello";
    public int a = 1;

    public function process() returns int {
        return 5 + a;
    }
};

public type LengthWindow object {
    public string v = "hello1";
    public int a = 2;

    public function process() returns int {
        return 10 + a;
    }
};

public type SampleWindow object {
    public string v = "hello2";
    public int a = 3;

    public function process() returns int {
        return 15 + a;
    }
};

function testObjectFunctionPointer() returns int {
    Window win = new LengthWindow();
    function () returns int pointer = win.process;
    win = new SampleWindow();
    return pointer();
}