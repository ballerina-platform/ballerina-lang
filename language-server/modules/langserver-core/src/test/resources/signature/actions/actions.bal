public function actions() returns error? {
    //==================================================
    // Actions
    //==================================================
    // action :=
    //    start-action
    //    | wait-action
    //    | send-action
    //    | receive-action
    //    | flush-action
    //    | remote-method-call-action
    //    | checking-action
    //    | trap-action
    //    | ( action )
    // action-or-expr := action | expression
    // checking-action := checking-keyword action
    // trap-action := trap action

    // -- action, start-action
    future<int> r1 = start foo(2, true);

    // -- action, wait-action
    int a = wait bar(3);

    // -- action, send-action
    // -- action, receive-action
    worker w1 {
        foo(1, false) ->> w2;
    }
    worker w2 {
        int lw = <- w1;
    }

    // -- action, flush-action
    // flush w2;

    // -- action, remote-method-call-action
    Stub stub = new(1);
    stub->send("");

    // -- action, checking-action
    int aa = check fooErr(2, false);
    int bb = checkpanic fooErr(1, false);

    // -- action, trap-action
    error? e = trap fooE(1, false);
}

# Function Foo
# + a - a float
# + b - b bool
# + return - an integer
function foo(float a, boolean b) returns int {
    return 1;
}

# Function Bar
#
# + n - n float
# + return - future integer
function bar(float n) returns future<int> {
    return start foo(2, false);
}

# Function Foo or Error
# + a - a float
# + b - b bool
# + return - an integer
function fooErr(float a, boolean b) returns (int | error) {
    return 1;
}

# Function Foo
# + a - a float
# + b - b bool
# + return - optional error
function fooE(float a, boolean b) returns error? {
    return error("");
}

public type Child object {
    # Returns foo
    # + a - float
    # + b - boolean
    public function foo(float a, boolean b) {

    }
};

public type Stub client object {

    public Child obj = new Child();

    # Create a new Stub
    public function init(any arg) {

    }

    # Send a message
    # + msg - message
    public remote function send(string msg) {

    }

    # Returns name
    # + a - float
    # + b - boolean
    public function foo(float a, boolean b) {

    }

    # Returns bar
    # + a - float
    # + b - boolean
    # + return - child
    public function bar(float a, boolean b) returns Child {
        return new Child();
    }


    # Returns bar
    # + a - float
    # + b - boolean
    # + return - child
    public function optionalBar(float a, boolean b) returns Child? {
        return ();
    }
};
