public class IntSet {
    map<boolean> set;

    public function init() {
        self.set = {};
    }

    public function add(int value) {
        self.set[self.toKey(value)] = true;
    }

    public function has(int value) returns boolean {
        boolean? hasValue = self.set[self.toKey(value)];
        return hasValue is () ? false : hasValue;
    }

    public function values() returns int[] {
        int[] accu = [];
        foreach var key in self.set.keys().sort() {
            accu.push(self.fromKey(key));
        }
        return accu;
    }

    public function remove(int value) {
        // check for existence first because removing non-existing value will panic
        if self.has(value) {
            var _ = self.set.remove(self.toKey(value));
        }
    }

    private function toKey(int value) returns string {
        return value.toString();
    }

    private function fromKey(string value) returns int {
        return checkpanic 'int:fromString(value);
    }
}
