class Iterable {
    public function __iterator() returns object {public function next() returns record {|int value;|}?;
    } {
        InternalIterable sample = new;
        return sample;
    }
}

public function testIterableObject() returns int[] {
    Iterable p = new Iterable();
    int[] integers = [];
    foreach var item in p {
        integers.push(item);
    }

    return integers;
}

class InternalIterable {
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
}

class AnotherIterable {
    public function __iterator() returns object {public function next() returns record {|Iterable value;|}?;
    } {
        AnotherInternalIterable sample = new;
        return sample;
    }
}

class AnotherInternalIterable {
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
}

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
