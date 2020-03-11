type Iterable object {
    public function __iterator() returns abstract object {public function next() returns record {|int value;|}?;
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns
            record {|
                int value;
            |}? {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 7) {
                    return {
                        value: self.integers[self.cursorIndex - 1]
                    };
                } else {
                    return ();
                }
            }
        } sample = new;
        return sample;
    }
};

public function testIterableObject() returns int[] {
    Iterable p = new Iterable();
    int[] integers = [];
    foreach var item in p {
        integers.push(item);
    }

    return integers;
}

type AnotherIterable object {
    public function __iterator() returns abstract object {public function next() returns record {|Iterable value;|}?;
    } {
        object {
            int cursorIndex = 0;
            public function next() returns
            record {|
                Iterable value;
            |}? {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 2) {
                    return {
                        value: new Iterable()
                    };
                } else {
                    return ();
                }
            }
        } sample = new;
        return sample;
    }
};

public function testNestedIterableObject() returns int[] {
    AnotherIterable p = new AnotherIterable();
    int[] integers = [];
    foreach var itr in p {
        foreach var item in itr {
            integers.push(item);
        }
    }

    return integers;
}

// ------------------- Error Related Tests -------------------

type CustomErrorData record {|
    string message?;
    error cause?;
    int errorID?;
|};

type CustomError error<string, CustomErrorData>;

type IterableWithCustomError object {
    public function __iterator() returns abstract object {public function next() returns record {|int value;|}|CustomError?;
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns
            record {|
                int value;
            |}|CustomError? {
                self.cursorIndex += 1;
                if (self.cursorIndex == 5) {
                    CustomError e = error("CustomError", message = "custom error occured", errorID = self.cursorIndex);
                    return e;
                } else if (self.cursorIndex <= 7) {
                    return {
                        value: self.integers[self.cursorIndex - 1]
                    };
                } else {
                    return ();
                }
            }
        } sample = new;
        return sample;
    }
};

public function testIterableObjectWithCustomError() returns boolean {
    int[] integers = [12, 34, 56, 34, 78, 21, 90];
    int cursorIndex = 0;
    boolean testPassed = true;
    IterableWithCustomError itr = new IterableWithCustomError();

    foreach var item in itr {
        if (cursorIndex == 4) {
            testPassed = testPassed && item is CustomError;
            testPassed = testPassed && item is error;
        } else {
            int i = integers[cursorIndex];
            testPassed = testPassed && i == <int>item;
        }
        cursorIndex += 1;
    }
    return testPassed;
}

type IterableWithGenericError1 object {
    public function __iterator() returns abstract object {public function next() returns record {|int value;|}|error?;
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns
            record {|
                int value;
            |}|error? {
                self.cursorIndex += 1;
                if (self.cursorIndex == 5) {
                    // returns a generic error
                    return error("GenericError", message = "generic error occured");
                } else if (self.cursorIndex <= 7) {
                    return {
                        value: self.integers[self.cursorIndex - 1]
                    };
                } else {
                    return ();
                }
            }
        } sample = new;
        return sample;
    }
};

public function testIterableObjectWithGenericError1() returns boolean {
    int[] integers = [12, 34, 56, 34, 78, 21, 90];
    int cursorIndex = 0;
    boolean testPassed = true;
    IterableWithGenericError1 itr = new IterableWithGenericError1();

    foreach var item in itr {
        if (cursorIndex == 4) {
            testPassed = testPassed && item is error;
        } else {
            int i = integers[cursorIndex];
            testPassed = testPassed && i == <int>item;
        }
        cursorIndex += 1;
    }
    return testPassed;
}

type IterableWithGenericError2 object {
    public function __iterator() returns abstract object {public function next() returns record {|int value;|}|error?;
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns
            record {|
                int value;
            |}|error? {
                self.cursorIndex += 1;
                if (self.cursorIndex == 5) {
                    // returns a custom error
                    CustomError e = error("CustomError", message = "custom error occured", errorID = self.cursorIndex);
                    return e;
                } else if (self.cursorIndex <= 7) {
                    return {
                        value: self.integers[self.cursorIndex - 1]
                    };
                } else {
                    return ();
                }
            }
        } sample = new;
        return sample;
    }
};

public function testIterableObjectWithGenericError2() returns boolean {
    int[] integers = [12, 34, 56, 34, 78, 21, 90];
    int cursorIndex = 0;
    boolean testPassed = true;
    IterableWithGenericError2 itr = new IterableWithGenericError2();

    foreach var item in itr {
        if (cursorIndex == 4) {
            testPassed = testPassed && item is CustomError;
            testPassed = testPassed && item is error;
        } else {
            int i = integers[cursorIndex];
            testPassed = testPassed && i == <int>item;
        }
        cursorIndex += 1;
    }
    return testPassed;
}
