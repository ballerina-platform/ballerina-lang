class Iterable {
    public function __iterator() returns object {

        public function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns record {|int value;|}? {
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

public function testIterableObject() returns int[] {
    Iterable p = new Iterable();
    int[] integers = from var item in p
                     select item;

    return integers;
}

class AnotherIterable {
    public function __iterator() returns object {

        public function next() returns record {|Iterable value;|}?;
    } {
        return object {
            int cursorIndex = 0;
            public function next() returns record {|Iterable value;|}? {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 2) {
                    return {
                        value: new Iterable()
                    };
                } else {
                    return ();
                }
            }
        };
    }
}

public function testNestedIterableObject() returns int[] {
    AnotherIterable p = new AnotherIterable();
    int[] integers = from var itr in p
                     from var item in itr
                     select item;

    return integers;
}

class IterableWithError {
    public function __iterator() returns object {

        public function next() returns record {|int value;|}|error?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78];
            int cursorIndex = 0;
            public function next() returns record {|int value;|}|error? {
                self.cursorIndex += 1;
                if (self.cursorIndex == 3) {
                    return error("Custom error thrown.");
                }
                else if (self.cursorIndex <= 5) {
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

public function testIterableWithError() returns int[]|error {
    IterableWithError p = new IterableWithError();
    int[]|error integers = from var item in p
                     select item;

    return integers;
}

class NumberGenerator {
    int i = 0;
    public isolated function next() returns record {| int value; |}? {
        self.i += 1;
        if(self.i < 5) {
          return { value: self.i };
        }
    }
}

class NumberStreamGenerator {
    int i = 0;
    public isolated function next() returns record {| stream<int> value; |}? {
         self.i += 1;
         if (self.i < 5) {
             NumberGenerator numGen = new();
             stream<int> numberStream = new (numGen);
             return { value: numberStream};
         }
    }
}

public function testStreamOfStreams() returns int[] {
    NumberStreamGenerator numStreamGen = new();
    stream<stream<int>> numberStream = new (numStreamGen);
    record {| stream<int> value; |}? nextStream = numberStream.next();
    int[] integers = from var strm in numberStream
                     from var num in liftError(toArray(strm))
                     select <int>num;

    return integers;
}

function toArray (stream<any|error, error> strm) returns any[]|error {
    any[] arr = [];
    record {| any|error value; |}|error? v = strm.next();
    while (v is record {| any|error value; |}) {
        any|error value = v.value;
        if (!(value is error)) {
            arr.push(value);
        }
        v = strm.next();
    }
    if (v is error) {
        return v;
    }
    return arr;
}

function liftError (any[]|error arrayData) returns any[] {
    if(!(arrayData is error)) {
        return arrayData;
    }
    return [];
}

public function testIteratorInStream() returns int[]|error {
    int[] intArray = [1, 2, 3, 4, 5];
    stream<int> numberStream = intArray.toStream();
    int[]|error integers = from var num in getIterableObject(numberStream.iterator())
                     select <int>num;

    return integers;
}

public type _Iterator object {
    public function next() returns record {|any|error value;|}|error?;
};

class IterableFromIterator {
        _Iterator itr;
        public function init(_Iterator itr) {
            self.itr = itr;
        }

        public function __iterator() returns _Iterator {
            return self.itr;
        }
}

function getIterableObject(_Iterator iterator) returns IterableFromIterator {
    return new IterableFromIterator(iterator);
}
