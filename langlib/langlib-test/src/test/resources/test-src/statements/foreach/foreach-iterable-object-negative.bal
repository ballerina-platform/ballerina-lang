type Iterable1 object {
    public function __iterator() returns abstract object {public function next() returns record {int value;}?;
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

type Iterable2 object {
    public function __iterator() returns abstract object {public function next() returns record {|int value;|};
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns
            record {|
                int value;
            |} {
                return {
                    value: self.integers[self.cursorIndex - 1]
                };

            }
        } sample = new;
        return sample;
    }
};

type Iterable3 object {
    public function __iterator() returns abstract object {public function next() returns record {|int x;|}?;
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns
            record {|
                int x;
            |} {
                return {
                    x: self.integers[self.cursorIndex - 1]
                };

            }
        } sample = new;
        return sample;
    }
};

type Iterable4 object {
    public function __iterator() returns abstract object {public function foo() returns record {|int value;|}?;
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function foo() returns
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

type Iterable5 object {
    public function _iterator() returns abstract object {public function next() returns record {|int value;|}?;
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

// ------------------- Error Related Tests -------------------
type CustomErrorData record {|
    string message?;
    error cause?;
    int errorID?;
|};

type CustomError error<string, CustomErrorData>;

type Iterable6 object {
    public function __iterator() returns abstract object {public function next() returns record {|int value;|}?;
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns
            record {|
                int value;
            |}|CustomError? {
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

type Iterable7 object {
    public function __iterator() returns abstract object {public function next() returns record {|int value;|}|CustomError?;
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

type Iterable8 object {
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

type Iterable9 object {
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

public function testIterableObject() {
    int[] integers = [];
    Iterable1 p1 = new;
    Iterable2 p2 = new;
    Iterable3 p3 = new;
    Iterable4 p4 = new;
    Iterable5 p5 = new;
    Iterable6 p6 = new;
    Iterable7 p7 = new;
    Iterable7 p8 = new;
    Iterable7 p9 = new;
    foreach var item in p1 {
    }
    foreach var item in p2 {
    }
    foreach var item in p3 {
    }
    foreach var item in p4 {
    }
    foreach var item in p5 {
    }
    foreach var item in p6 {
    }
    foreach var item in p7 {
    }
    foreach var item in p8 {
        integers.push(item);
    }
    foreach var item in p9 {
        integers.push(item);
    }
}
