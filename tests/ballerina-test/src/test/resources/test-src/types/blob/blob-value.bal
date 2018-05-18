import ballerina/io;

blob glbVarBlob;

blob gA = base16 `aeeecdefabcd12345567888822`;
blob gB = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;

type BinaryData {
    blob blobField;
};

type TestRec {
    blob a = base16 `aa ab`;
    blob b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    blob c;
};

type TestObj object {
    public {
        blob a = base16 `aa ab`;
        blob b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
        blob c;
    }
};

function base16Test() {
    blob a = base16 `aa ab cc ad af df 1a d2 f3 a4`;
    blob b = base16 `aaab ccad afcd 1a4b abcd 12df 345d`;
    blob c = base16 `aaabcfccad afcd34 1a4bdf abcd8912df`;
    blob d = base16 `aaabcfccad afcd34 1a4bdf abcd8912df`;
    blob e = base16 `afcd341a4bdfaaabcfccadabcd89 12df`;
    blob f = base16 `afcd34abcdef123abc234bcd1a4bdfaaabadabcd892312df`;
    blob x = base16 ``;
    blob y = base16`12ac 34f c56bcdf 78`;
    blob z = base16 `      `;
}

function base64Test() {
    blob a = base64 `aa ab cc ad af df 1a d2 f3 a4`;
    blob b = base64 `  aaabccadafcd 1a4b abcd12dff45d`;
    blob c = base64 `aaabcfccad afcd3 4bdf abcd ferf =`;
    blob d = base64 `aaabcfccad afcd34 1a4bdf abcd8912df kmk=`;
    blob e = base64 `afcd3  41a4bdfaaabcfccadabcd89 12df ss==`;
    blob f = base64 `afcd34abcdef123aGc 234bcd1a4bdfABbadaBCd892s3as==`;
    blob g = base64 `aGVd34abcdef+dfg123abc234bcd1a4bdfaaabadabcd8923as==`;
    blob h = base64 `aFRdfvfF34aVFdef+dfg123abc234cd/1a4bdfaaabadabcd8923as==`;
    blob i = base64 `afcd34aBNhIbF  Ref+dfg123ab+w234bcd1a4bdfaTNJGdabcd8923as=`;
    blob j = base64 `afcd34abcdef+dfginermkmf123w/bc234cd/1a4bdfaaFGTdaKMN8923as=`;
    blob x = base64 ``;
    blob y = base64`aFRdfvfF34aVFdef+dfg12334cd/1a4bdfaaabadabcd8923as==`;
    blob z = base64 `    `;
}

function testBlobParameter(blob b) returns (blob) {
    blob a;
    a = b;
    return a;
}

function testBlobReturn() returns (blob) {
    blob a = base64 `aa ab cc ad af df 1a d2 f3 a4`;
    return a;
}

function testGlobalVariable1(blob b) returns (blob) {
    glbVarBlob = b;
    return glbVarBlob;
}

function testGlobalVariable2() returns (blob) {
    glbVarBlob = gA;
    return glbVarBlob;
}

function testGlobalVariable3() returns (blob) {
    blob a = gB;
    return a;
}

function testBlobParameterArray(blob b1, blob b2) returns (blob, blob) {
    blob [] a = [];
    a[0] = b1;
    a[1] = b2;
    return (a[0], a[1]);
}

function testBlobReturnTuple1() returns (blob, blob) {
    return testBlobParameterArray(base16 `aaabafac23345678`, base64 `a4f5njn/jnfvr+d=`);
}

function testBlobReturnTuple2() returns (blob, blob, blob, blob) {
    var (a, b) = testBlobParameterArray(base16 `aaab`, base64 `a4f5`);
    return (a, b, gA, gB);
}

function testBlobReturnArray() returns (blob[]) {
    blob [] a = [];
    a[0] = base16 `aaab34dfca1267` ;
    a[1] = base64 `aaabcfccadafcd34bdfabcdferf=`;
    return a;
}

function testBlobField1(blob b) returns (blob) {
    BinaryData bytes = {blobField:b};
    return bytes.blobField;
}

function testBlobField2() returns (blob) {
    blob b = base16 `aaabcfccadafcd341a4bdfabcd8912df`;
    BinaryData bytes = {blobField:b};
    return bytes.blobField;
}

function testBlobHexFormat() returns (string, string, string) {
    blob a = base16 `aaabcfccadafcd341a4bdfabcd8912df`;
    blob b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    string blobStr1 = io:sprintf("%x", a);
    string blobStr2 = io:sprintf("%X", a);
    string blobStr3 = io:sprintf("%x", b);

    return(blobStr1, blobStr2, blobStr3);
}

function testBlobAssign() returns (blob, blob) {
    blob a = base16 `aaabcfccadafcd341a4bdfabcd8912df`;
    blob b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    blob c = a;
    a = b;
    b = c;
    return (a, b);
}

function testBlobDefaultValue() returns (blob, blob, blob, blob, blob, blob, blob, blob) {
    blob a;
    blob b = a;
    TestRec testRec = {};
    TestObj testObj = new;
    return (a, b, testRec.a, testRec.b, testRec.c, testObj.a, testObj.b, testObj.c);
}
