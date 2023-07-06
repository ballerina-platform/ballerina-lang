function base16InvalidLiteralTest() {
    byte[] b = base1 6 `aa ab cc ad af df 1a d2 f3 a4`;
    byte[] h = base 16 `aaab ccad afcd 1a4b abcd 12df 345`;
    byte[] e = base16 `aaabcfccad afcd34 a4bdfaq abcd8912df`;
    byte[] d = base16 `!wewerfjnjrnf12`;
    byte[] f = base16 `afcd341a4bdfaaabcfccadabcd89 12df =`;
    byte[] g = base16 `ef1234567mmkmkde`;
    byte[] c = base16 "aeeecdefabcd12345567888822";
}

function base64InvalidLiteralTest() {
    byte[] a = base6 4 `aa ab cc ad af df 1a d2 f3 a4`;
    byte[] b = base 64 `aaabccadafcd 1a4b abcd12dff45d`;
    byte[] c = base64 `aaabcfccad afcd3 4bdf abcd ferf $$$$=`;
    byte[] d = base64 `aaabcfccad afcd34 1a4bdf abcd8912df kmke=`;
    byte[] e = base64 "afcd34abcdef123aGc234bcd1a4bdfABbadaBCd892s3as==";
    byte[] f = base64 `afcd341a4bdfaaabmcfccadabcd89 12df ss==`;
    byte[] g = base64 `afcd34abcdef123aGc2?>><*&*^&34bcd1a4bdfABbadaBCd892s3as==`;
}

function byteArrayLiteralTypeTest() {
    byte[2] a = base16 `aa bb`;
    byte[3] b = base16 `aa bb`;             // error
    int[3] c = base16 `aa bb`;              // error
    byte[*] d = base16 `aa bb`;
    int[*] e = base16 `aa bb`;

    byte[2] f = base64 `aa bb`;             // error
    byte[3] g = base64 `aa bb`;
    int[2] h = base64 `aa bb`;              // error
    byte[*] i = base64 `aa bb`;
    int[*] j = base64 `aa bb`;
    byte[] k = <byte[3] & readonly>base16 `aa bb`; // error
    int[] l = <int[2] & readonly>base64 `aa bb`; // error
    string[] m = <string[] & readonly>base64 `aa bb`; // error

    var n = base16 `aa bb`;
    byte[2] _ = n;
    byte[3] _ = n;                          // error
}
