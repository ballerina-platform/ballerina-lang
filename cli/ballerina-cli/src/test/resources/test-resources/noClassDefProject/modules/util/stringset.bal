public class StringSet {
    map<boolean> set;

    public function init() {
        self.set = {};
    }

    public function add(string value) {
        self.set[value] = true;
    }

    public function has(string value) returns boolean {
        boolean? hasValue = self.set[value];
        return hasValue is () ? false : hasValue;
    }

    public function values() returns string[] {
        string[] accu = [];
        foreach var key in self.set.keys().sort() {
            accu.push(key);
        }
        return accu;
    }

    public function remove(string value) {
        // check for existence first because removing non-existing value will panic
        if self.has(value) {
            var _ = self.set.remove(value);
        }
    }

    public function toString() returns string {
        return self.values().toString();
    }
}

public function union(StringSet a, StringSet b) returns StringSet {
    StringSet c = new;
    foreach var item in a.values() {
        c.add(item);
    }
    foreach var item in b.values() {
        c.add(item);
    }
    return c;
}

public function intersection(StringSet a, StringSet b) returns StringSet {
    StringSet c = new;
    foreach var item in a.values() {
        if b.has(item) {
            c.add(item);
        }
    }
    foreach var item in b.values() {
        if a.has(item) {
            c.add(item);
        }
    }
    return c;
}

// a - b = c
public function difference(StringSet a, StringSet b) returns StringSet {
    StringSet c = new;
    foreach var item in a.values() {
        if !b.has(item) {
            c.add(item);
        }
    }
    return c;    
}