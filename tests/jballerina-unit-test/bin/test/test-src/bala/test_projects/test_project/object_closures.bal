public final int x = 300;
public final int y = 200;

public function createObject() returns object {
    public int number;
    public function getNumber() returns int;
} {
    object {
        public int number;
        public function getNumber() returns int;
    } obj = object {
        public int number = x;

        public function getNumber() returns int {
            return self.number + y;
        }
    };
    return obj;
}
