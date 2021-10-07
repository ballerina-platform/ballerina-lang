import ballerina/module1;

function main(TestRec rec) {
    rec.rec2.content.toHexString();
}

type TestRec record {
    int content;
    TestRec2 rec2;
};

type TestRec2 record {
    int content;
};
