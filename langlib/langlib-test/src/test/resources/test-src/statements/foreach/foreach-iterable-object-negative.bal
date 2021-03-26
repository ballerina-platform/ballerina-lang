class Iterable1 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
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
        };
    }
}

class Iterable2 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
            record {|
                int value;
            |} {
                return {
                    value: self.integers[self.cursorIndex - 1]
                };

            }
        };
    }
}

class Iterable3 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
            record {|
                int value;
            |} {
                return {
                    value: self.integers[self.cursorIndex - 1]
                };

            }
        };
    }
}

class Iterable4 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns record {|int value;|}?
            {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 7) {
                    return {
                        value: self.integers[self.cursorIndex - 1]
                    };
                } else {
                    return ();
                }
            }
        };
    }
}

class Iterable5 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
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
        };
    }
}

// ------------------- Error Related Tests -------------------
type CustomErrorData record {|
    string message?;
    error cause?;
    int errorID?;
|};

type CustomError error<CustomErrorData>;

class Iterable6 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
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
        };
    }
}

class Iterable7 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}|CustomError?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
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
        };
    }
}

class Iterable8 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}|error?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
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
        };
    }
}

class Iterable9 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int value;|}|CustomError?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
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
        };
    }
}

public function testIterableObject() {
    int[] integers = [];
    Iterable1 p1 = new;
    Iterable2 p2 = new;
    Iterable3 p3 = new;
    Iterable4 p4 = new;
    Iterable5 p5 = new;
    Iterable6 p6 = new;
    Iterable7 p7 = new;
    Iterable8 p8 = new;
    Iterable9 p9 = new;
    Iterable13 p13 = new;
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
    foreach var item in p13 {
    }
}

class Iterable10 {
    *object:Iterable;
    public function iterator() returns object {public function foo() returns record {|int value;|}?;
    } {
        return object {
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
        };
    }
}

class Iterable11 {
    *object:Iterable;
    public function _iterator() returns object {public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
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
        };
    }
}

class Iterable12 {
    *object:Iterable;
    public function iterator() returns object {public isolated function next() returns record {|int x;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
            record {|
                int x;
            |} {
                return {
                    x: self.integers[self.cursorIndex - 1]
                };

            }
        };
    }
}

class Iterable13 {
    public function iterator() returns object {public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns
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
        };
    }
}
