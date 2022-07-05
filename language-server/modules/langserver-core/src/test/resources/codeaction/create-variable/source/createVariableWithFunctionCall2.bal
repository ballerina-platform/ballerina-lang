type MyRec record {|
    int count;
|};

function getRec() returns record {| int a; |} {
    return {
        a: 0
    };
}

public function main() {
    getRec();
}
