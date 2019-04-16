import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function which will replace the real function
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
}

@test:Config { }
function testFunc() {
    // Invoking the main function
    var ret = main();
    test:assertEquals(outputs.length(), 25);
    test:assertEquals(outputs[0], "Hex encoded hash with MD5: 0605402EE16D8E96511A58FF105BC24A");
    test:assertEquals(outputs[1], "Base64 encoded hash with SHA1: /8fwbGIevBvv2Nl3gEL9DtWas+Q=");
    test:assertEquals(outputs[2], "Hex encoded hash with SHA256: A984A643C350B17F0738BAC0FEF17F2CD91D91E04596351D0AF" +
                                  "670C79ADC12D5");
    test:assertEquals(outputs[3], "Base64 encoded hash with SHA384: lselzItgAZHQmqNbkf/s2aRjBSd93O3ayc0PB0Dxk6AEo1s4" +
                                  "4zyTz/Qp0FJO1n6b");
    test:assertEquals(outputs[4], "Hex encoded hash with SHA512: A6F0770F1582F49396A97FBD5973AC22A3A578AC6A991786427" +
                                  "DFEC17DBD984D8D6289771AC6F44176160A3DCD59F4A8C6B3AB97BEF0CAA5C67A3FAC78C8E946");
    test:assertEquals(outputs[5], "Hex encoded HMAC with MD5: B69FA2CC698E0923A7EEA9D8F2B156FE");
    test:assertEquals(outputs[6], "Base64 encoded HMAC with SHA1: AkWFajkb/gml703Zf4pPgxrjam4=");
    test:assertEquals(outputs[7], "Hex encoded HMAC with SHA256: 13A3369B8BA326FD311D4500B06A5214A02ED2A033917108F6B" +
                                  "9AF58B7EDE381");
    test:assertEquals(outputs[8], "Base64 encoded HMAC with SHA384: 0AjKoWLhNPgdobGTPJs0PdkA0W9wkJtzUvXigzC1ZmXDJJsx" +
                                  "p4icks4JrPiwHGm6");
    test:assertEquals(outputs[9], "Hex encoded HMAC with SHA512: 27588AD08E772A6BBA5FCA5F45CF467819C8DE69A70A42BE6FE" +
                                  "3EB09CEB3BFEB8B2976BDA8EA5C10DCFA2294B12CB0B50B22A06309BADA98AF21857904A03205");
    test:assertEquals(outputs[10], "CRC32B for text: db9230c5");
    test:assertEquals(outputs[11], "CRC32 for xml content: 7d5c0879");
    test:assertEquals(outputs[12], "Hex encoded RSA-MD5 signature: 2CFD121E4FF2409D1B2482EBBF37D0C035884D6D858E307E4" +
                                   "460B092D79CB20ABB624A0DFAE76B73B1FC85447BE3060A36B318813F0115B1919E5EFA7A7F9B117" +
                                   "3EC869F56FD9448D99770E1565DB1C69A04FD0075FA0E33423A7E829A8B9C25A4DD2C68F3EEE021C" +
                                   "0C4FF27979750B395384E280AFD87AF5274C8D2D99AD4438D9BFC9B2C5A2814212BA29CE6FF70CBE" +
                                   "30A5C23F86B0330E143C4D8813FF10092CD313C6861706D37DF5F4BB4E9FC72354975EE1786CF24C" +
                                   "79B9EDFA909968F198C4DA37464F3D214A68FB39956717E92D667BB5A9A7F5986BA055D431813D40" +
                                   "53A028873499F98C94FD6B5C6FD5AEFAD432669F957AB4CE9E91C5E77B36EC0");
    test:assertEquals(outputs[13], "Base64 encoded RSA-SHA1 signature: bYMHKKVkjbOUp9ly3AdW9/euxF94krkkF9SDk2FfbVEc0" +
                                   "mqpGIxVoZlPiFxszurZF1YPcqOSeOehDkaHXUMfQkTjBq7XlcePtkywy0fChqw8/SWqZR8nbYv97tt8+" +
                                   "MVTkymbm26syQLwYNLiGp/EsN6X+fJpkdakwHE+TrdE+rEFrNysGyAm1DWwc4c+l7MEmSYMUnh/GWPY5" +
                                   "r2knOOdDA3mr+XyrsxzFRWZgO7ebVvEQfq9XkRp8kdiGVgpLS5au0jKj3EpbCdS1prFgy3grkuSJTTUQ" +
                                   "CwgPo7WSjWbuehFGni7rbas8HIqNlyWF0qUyznJ3eqbUwZ95QqOoVWZoQ==");
    test:assertEquals(outputs[14], "Hex encoded RSA-SHA256 signature: 215C6EA96C9E82348430C6BB02E715560B4FBD3AFCF24F" +
                                   "BEB41FF12D4D68A797D61C4D6F822807688E4DC604E212B3CC7AC563B3CBE4E5690E2AEBAF4E3DF3" +
                                   "5C19D4B0F7043F50501F390634303577053B029D495104C0E98BC887F0BE744EF6F726F719201907" +
                                   "AD4E86CEF82EB030B60C384F7034A85159081E598E197BB8904A9123F39D190796DC7FD946157547" +
                                   "C10523999B8FA956D4119DBFE3C1435911C0585CF3C537964516706772E87F247055740CC4867AC6" +
                                   "B99D7BF699FCE1B59956C7F55368C8C88C9D47E51EF120ED3F27C3E555691A697142C78CBD72C23B" +
                                   "81B43FA5AB67164A35F8E8C6BF1DA187D3FEB866ADD13F1FB9576A2F7887535311");
    test:assertEquals(outputs[15], "Base64 encoded RSA-SHA384 signature: BjQ40dffGiRQ4zo1s+ld+zKhJL21RbO5sW3L2+4xmon" +
                                   "Ut126u9D4/FZ2sM1QGGamj8btB9otiYmWr9sFm4fTs1EX6vrxcCGCAiDdkMxiRs7kShaz2x/BjJQ7cOd" +
                                   "9OY+amwo7DQ/FAk9mNOt4lFUpjc9WyEW9F1PEJRXZQvMmVabDu8lp/Fh02lmEquG15DT5qT0jRxRJiS8" +
                                   "CNa+97cMZdOmF2KeADfRbNJSz70mZ76MrsNxYIXYIiJzJBQod0efQr0Sr/HDn4JDVph9rpDM3p8m94Ty" +
                                   "XvSOwxwxzZWRLEwB0ANdfDmbrW4bOpxfZZFmy1hltqNJQ9G0BcKOHsZDj6Q==");
    test:assertEquals(outputs[16], "Hex encoded RSA-SHA512 signature: 15428FDC7B26D18B1F3EAE4569399AE6EBFD430C8F073B" +
                                   "F2FA77EBFE1AD5645640374EA4A4AEADD252AF3A198E55E69AD2A910E28470D9B54748887DE06A5C" +
                                   "3ED7AB12399A404359332553E051E8AE0F3EF741FAA15A21AD17A9C235E5F91D567BCCA0E5A61176" +
                                   "89DCCADA4A33EE897514F7A8A32F12DAC0087F5DCBB094C93C792F672E1685618AC5D93AA9D30F6D" +
                                   "8E306145EF2D1B9CFDC04D6C61B43376089A78471E8E03D97EE3B57E1B734A23F44366A99234A0AB" +
                                   "EB1D36D01C474833B4C2BEAF430DAE06AB95A1C951645FB1E0A5E7B9EED44D40E35036F2CD2764DF" +
                                   "6CC04FE1248E1BB772A53C8201A974109333A318CE57930494D4CB5E41D0DC8F1C");
    test:assertEquals(outputs[17], "AES CBC PKCS5 decrypted value: Hello Ballerina!");
    test:assertEquals(outputs[18], "AES CBC no padding decrypted value: Hello Ballerina!");
    test:assertEquals(outputs[19], "AES GCM PKCS5 decrypted value: Hello Ballerina!");
    test:assertEquals(outputs[20], "AES GCM no padding decrypted value: Hello Ballerina!");
    test:assertEquals(outputs[21], "AES ECB PKCS5 decrypted value: Hello Ballerina!");
    test:assertEquals(outputs[22], "AES ECB no padding decrypted value: Hello Ballerina!");
    test:assertEquals(outputs[23], "RSA ECB PKCS1 decrypted value: Hello Ballerina!");
    test:assertEquals(outputs[24], "RSA ECB OAEPwithSHA512andMGF1 decrypted value: Hello Ballerina!");
}
