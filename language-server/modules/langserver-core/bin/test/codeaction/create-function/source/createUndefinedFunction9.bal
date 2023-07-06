public class Counter {
    private Counter n;

    public function init(Counter n) {
        self.n = n;
    }
}

public function main () {
   Counter c = getCounter();
}
