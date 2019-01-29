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
