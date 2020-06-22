import ballerina/io;
import ballerina/runtime;

public function main() {
    worker w1 {
        int i = 100;
        float k = 2.34;
        [i, k] -> w2;

        json j = {
        };
        j = <- w2;

        () send1 = i ->> w2;
        k -> w3;
        k -> w3;
        k -> w3;

        () send2 =
            i
            ->>
            w2
        ;

        () send3 =
            i
            ->>
            default
        ;

        () send4 = i ->> default;

        error? flushResult1 = flush w3;
        error? flushResult2 = flush;

        error? flushResult3 =
            flush
            w2
        ;
        error? flushResult4 =
            flush
        ;
    }

    worker w2 {
        int iw;
        float kw;
        [int, float] vW1 = [0, 1.0];
        vW1 = <- w1;
        [iw, kw] = vW1;

        json jw = {"name": "Ballerina"};
        jw -> w1;

        int lw;
        runtime:sleep(5);
        lw = <- w1;
    }

    worker w3 {
        float mw;
        runtime:sleep(50);
        mw = <- w1;
        mw = <- w1;
        mw = <- w1;
    }

    worker w4 {
        map<string> keyMap = {myKey: "abc123"};
        xml msg = xml `<msg><name>ballerina</name><worker>w2</worker></msg>`;
    }
}
