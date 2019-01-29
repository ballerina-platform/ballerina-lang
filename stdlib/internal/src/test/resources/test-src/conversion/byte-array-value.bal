import ballerina/io;

byte[] glbVarBlob = [];

byte[] gA = base16 `aeeecdefabcd12345567888822`;
byte[] gB = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;

type BinaryData record {
    byte[] blobField;
};

type TestRec record {
    byte[] a = base16 `aa ab`;
    byte[] b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    byte[] c = [];
};

type TestObj object {
    public byte[] a = base16 `aa ab`;
    public byte[] b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    public byte[] c = [];
};

function base16Test() {
    byte[] a = base16 `aa ab cc ad af df 1a d2 f3 a4`;
    byte[] b = base16 `aaab ccad afcd 1a4b abcd 12df 345d`;
    byte[] c = base16 `aaabcfccad afcd34 1a4bdf abcd8912df`;
    byte[] d = base16 `aaabcfccad afcd34 1a4bdf abcd8912df`;
    byte[] e = base16 `afcd341a4bdfaaabcfccadabcd89 12df`;
    byte[] f = base16 `afcd34abcdef123abc234bcd1a4bdfaaabadabcd892312df`;
    byte[] x = base16 ``;
    byte[] y = base16`12ac 34f c56bcdf 78`;
    byte[] z = base16 `      `;
}

function base64Test() {
    byte[] a = base64 `aa ab cc ad af df 1a d2 f3 a4`;
    byte[] b = base64 `  aaabccadafcd 1a4b abcd12dff45d`;
    byte[] c = base64 `aaabcfccad afcd3 4bdf abcd ferf =`;
    byte[] d = base64 `aaabcfccad afcd34 1a4bdf abcd8912df kmk=`;
    byte[] e = base64 `afcd3  41a4bdfaaabcfccadabcd89 12df ss==`;
    byte[] f = base64 `afcd34abcdef123aGc 234bcd1a4bdfABbadaBCd892s3as==`;
    byte[] g = base64 `aGVd34abcdef+dfg123abc234bcd1a4bdfaaabadabcd8923as==`;
    byte[] h = base64 `aFRdfvfF34aVFdef+dfg123abc234cd/1a4bdfaaabadabcd8923as==`;
    byte[] i = base64 `afcd34aBNhIbF  Ref+dfg123ab+w234bcd1a4bdfaTNJGdabcd8923as=`;
    byte[] j = base64 `afcd34abcdef+dfginermkmf123w/bc234cd/1a4bdfaaFGTdaKMN8923as=`;
    byte[] x = base64 ``;
    byte[] y = base64`aFRdfvfF34aVFdef+dfg12334cd/1a4bdfaaabadabcd8923as==`;
    byte[] z = base64 `    `;
}

function testByteArrayLiteral() returns byte[] {
    byte[] a = [1,27,34,145,224];
    return a;
}

function testBlobParameter(byte[] b) returns (byte[]) {
    byte[] a = [];
    a = b;
    return a;
}

function testBlobReturn() returns (byte[]) {
    byte[] a = base64 `aa ab cc ad af df 1a d2 f3 a4`;
    return a;
}

function testGlobalVariable1(byte[] b) returns (byte[]) {
    glbVarBlob = b;
    return glbVarBlob;
}

function testGlobalVariable2() returns (byte[]) {
    glbVarBlob = gA;
    return glbVarBlob;
}

function testGlobalVariable3() returns (byte[]) {
    byte[] a = gB;
    return a;
}

function testBlobParameterArray(byte[] b1, byte[] b2) returns (byte[], byte[]) {
    byte[] [] a = [];
    a[0] = b1;
    a[1] = b2;
    return (a[0], a[1]);
}

function testBlobReturnTuple1() returns (byte[], byte[]) {
    return testBlobParameterArray(base16 `aaabafac23345678`, base64 `a4f5njn/jnfvr+d=`);
}

function testBlobReturnTuple2() returns (byte[], byte[], byte[], byte[]) {
    var (a, b) = testBlobParameterArray(base16 `aaab`, base64 `a4f5`);
    return (a, b, gA, gB);
}

function testBlobReturnArray() returns (byte[][]) {
    byte[][] a = [];
    a[0] = base16 `aaab34dfca1267` ;
    a[1] = base64 `aaabcfccadafcd34bdfabcdferf=`;
    return a;
}

function testBlobField1(byte[] b) returns (byte[]) {
    BinaryData bytes = {blobField:b};
    return bytes.blobField;
}

function testBlobField2() returns (byte[]) {
    byte[] b = base16 `aaabcfccadafcd341a4bdfabcd8912df`;
    BinaryData bytes = {blobField:b};
    return bytes.blobField;
}

function testBlobHexFormat() returns (string, string, string) {
    byte[] a = base16 `aaabcfccadafcd341a4bdfabcd8912df`;
    byte[] b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    string blobStr1 = io:sprintf("%x", a);
    string blobStr2 = io:sprintf("%X", a);
    string blobStr3 = io:sprintf("%x", b);

    return(blobStr1, blobStr2, blobStr3);
}

function testBlobAssign() returns (byte[], byte[]) {
    byte[] a = base16 `aaabcfccadafcd341a4bdfabcd8912df`;
    byte[] b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    byte[] c = a;
    a = b;
    b = c;
    return (a, b);
}

function testBlobDefaultValue() returns (byte[], byte[], byte[], byte[], byte[], byte[], byte[], byte[]) {
    byte[] a = [];
    byte[] b = a;
    TestRec testRec = {};
    TestObj testObj = new;
    return (a, b, testRec.a, testRec.b, testRec.c, testObj.a, testObj.b, testObj.c);
}

function testByteArrayReturn() returns (byte[], byte[], byte[]) {
    byte[] a = base16 `aaabcfccadafcd341a4bdfabcd8912df`;
    byte[] b = base64 `aGVsbG8gYmFsbGVyaW5hICEhIQ==`;
    byte[] c = [3,4,5,6,7,8,9];
    return (a, b, c);
}
