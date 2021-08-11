type LoopContext record {|
    LoopContext? outer;
    string str;
    boolean ascending;
    boolean descending;
|};

public function main() {
    LoopContext ctx = {outer: (), str: "ABC", ascending: true, descending: false};
    int a = join;
    int limit = 5;
    match order {
        by => {}
        select => {}
    }
}
