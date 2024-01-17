int x = 5;
public function main() {
    func();

}

function func() {
    @strand {thread: "any"}
    worker A {
        1 ->> B;
    }

    @strand {thread: "any"}
    worker B returns error?{
        x-=1;
        if (x>1) {
            return error("err");
        }
        int x1 = <- A;
    }

}
