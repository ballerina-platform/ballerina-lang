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


public function testIterableObject() {
    Iterable1 p1 = new;
    Iterable2 p2 = new;
    Iterable3 p3 = new;
    Iterable4 p4 = new;
    Iterable5 p5 = new;
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
}
