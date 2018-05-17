function base16InvalidLiteralTest() {
    blob b = base1 6 `aa ab cc ad af df 1a d2 f3 a4`;
    blob h = base 16 `aaab ccad afcd 1a4b abcd 12df 345`;
    blob e = base16 `aaabcfccad afcd34 a4bdfaq abcd8912df`;
    blob d = base16 `!wewerfjnjrnf12`;
    blob f = base16 `afcd341a4bdfaaabcfccadabcd89 12df =`;
    blob g = base16 `ef1234567mmkmkde`;
    blob c = base16 "aeeecdefabcd12345567888822";
}

function base64InvalidLiteralTest() {
    blob a = base6 4 `aa ab cc ad af df 1a d2 f3 a4`;
    blob b = base 64 `aaabccadafcd 1a4b abcd12dff45d`;
    blob c = base64 `aaabcfccad afcd3 4bdf abcd ferf #$$$=`;
    blob d = base64 `aaabcfccad afcd34 1a4bdf abcd8912df kmk=`;
    blob e = base64 "afcd34abcdef123aGc234bcd1a4bdfABbadaBCd892s3as==";
    blob f = base64 `afcd341a4bdfaaabmcfccadabcd89 12df ss==`;
    blob g = base64 `afcd34abcdef123aGc2?>><*&*^&34bcd1a4bdfABbadaBCd892s3as==`;
}
