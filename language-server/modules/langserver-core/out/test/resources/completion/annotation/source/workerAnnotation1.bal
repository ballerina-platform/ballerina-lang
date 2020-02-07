const annotation v1 on source worker;

public function main() {
    @
    worker w1 {
        
    }

    future<int> f = @v1 start foo();
}

function foo() returns int {
    return 1;
}