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
    test:assertEquals(outputs.length(), 17);
    test:assertEquals(outputs[0], "Hex encoded hash with MD5: B7CDF48DCB2072D96987194301C9BB29");
    test:assertEquals(outputs[1], "Base64 encoded hash with SHA1: zWtjF5XdRjnbt5Bb3h3tCLxvwfk=");
    test:assertEquals(outputs[2], "Hex encoded hash with SHA256: 470C840787F9A5B453534EE0565C0992D76CA9100FE8A1B6F" + 
                                  "67834A79C0C749E");
    test:assertEquals(outputs[3], "Base64 encoded hash with SHA384: /Y+2DRQn4GMnxdh3O353e5wrpZDVEaA8gpVVLhPGKmtjy8" +
                                  "79GjM7tvlhWTf9v+SY");
    test:assertEquals(outputs[4], "Hex encoded hash with SHA512: A67035FC19AC1CCEE90DB79D8C06BC51938A45F9BDBBD9ADC" +
                                  "2BAAA1AB02068543A98A6A8381CE32801FB2557C95B9FC05D120CF8B10124709A8037896CE53A31");
    test:assertEquals(outputs[5], "Hex encoded HMAC with MD5: BD7BAF052B378B02B199ED3A48C24172");
    test:assertEquals(outputs[6], "Base64 encoded HMAC with SHA1: H5srwqG4xiHIeBMRYGSTM6nGpLI=");
    test:assertEquals(outputs[7], "Hex encoded HMAC with SHA256: 01699B5BD8E2EAD41FB011817C688E9F1109966FF652B156FC" +
                                  "D1FBA0E29D58E0");
    test:assertEquals(outputs[8], "Base64 encoded HMAC with SHA384: XZPhJQd7T9NjsVKxJFjvGGk1YkrlFU9DlMGZ3TV3eof5fEk" +
                                  "zg+OJkLdu0tEOm45U");
    test:assertEquals(outputs[9], "Hex encoded HMAC with SHA512: 283EE4F6A6D0E284F1391AF382764CABBE6752CF77C021F862" +
                                  "E2178485B957B444C109B0CB19A9444F87D61CEFC638E4C304F92AA97A1CADC510BD4D964D0C62");
    test:assertEquals(outputs[10], "CRC32B for text: 2693632d");
    test:assertEquals(outputs[11], "CRC32 for xml content: 7d5c0879");
    test:assertEquals(outputs[12], "Hex encoded RSA-MD5 signature: 72DCB9830B078303A229E47DDBEAE458FCC26EAEC277CB807" +
                                   "BAA5F3A502D2EFD2DC42F1C83C26C46BB64D4787C93892AD8AF1116FBE56C7A2FD32A2253C33D60F" +
                                   "C08BD44B5981874D6EA8CFE6F3DFCBF3DE69F7B5E6C6DA635C2823C02A264AB8BC139B9C9005A4F2" +
                                   "1AFA412202004FC9247BD0B60882F52BAEFCE07E336E5FAF8E7D24A08803296076C00FFE0A1EF405" +
                                   "D26F43EACF8F0DBEFAF9F4F1157A3BE28F31B3E7C89905C78793397A5F3342F161EC4F5C7DB60D11" +
                                   "7EE707394A3B65CEC14DC8817E03E0CEF6B66C6FD0D14EE9E95E08E015792BF976FF15D25DF0A0C7" +
                                   "70772CCD17A731A6FE5A77E613616B53D78C92FFE909A8D48CBADCD39B4FCB3");
    test:assertEquals(outputs[13], "Base64 encoded RSA-SHA1 signature: D6PU5+SPqq0RZNUJe2K9+2zLWdg4NeDi8blRuK1e4WBmy" +
                                   "4yjiWtXjxbAnhIpVuJAUDTTh2/0Bu8P8suFzkTZHFpCu3SkG9HnoDpnvdDTelEcwGViBSAkA2XR1ELDB" +
                                   "KqvYIcXJdRVliA1CY4LTKtysrGE57iOHZXbmfyiZLLedc2EOO8sT22dzSmd8aQTNWrHcYUEVTa7Sv/sd" +
                                   "6wFceEilfRyv49yvkuVDBi6xhyUnKR8TIcJ07lP3eBfP58olGWtS76GglOzHuQuNd5mLY0WykvCFoM9q" +
                                   "mqrJLJrONdAuILJOoqXnfeLpyvvWyiSfd8MPx6KomGOuD/WYCQKaL/xGA==");
    test:assertEquals(outputs[14], "Hex encoded RSA-SHA256 signature: 1FE7A13028507D0AFF1F5BFC866507AB9AEC6D690CD414" +
                                   "D756481A1C54D01E5318FC6CA801EC4E6A70DD9AD359C9312DE2A5EE9C300D904A71418E709B0FFF" +
                                   "10968E9C443C25CAF2A5D956CD26543C479E7F47562C8B1762DDE0159A420C3276167E678D2A4197" +
                                   "A663C653BF3BBF78AF304A3C1A36626BB328014E99FA6A84A442F19E28BC295DD45348027D5BEFC7" +
                                   "63E71DA42D872124B028C1CCBCC981DFCAA5B0C33AF0254296899DA4A625BA4FB7EEB8941E9A2827" +
                                   "88B14569732E2941AAC20E2B700AC5CBF372EEEED4032B1EAAC86CA234941843BA30ECA21090AF56" +
                                   "EEC0517BB67B2DF84E22220A81E146638064D5E79F44D10B5211F92362C65FA8E9");
    test:assertEquals(outputs[15], "Base64 encoded RSA-SHA384 signature: MhB9YczHBxv8YapgIonZDfuAJgWj/tEnwRlyVI0gd3i" +
                                   "4GHkvF3CIpiuBgv+PDP+rwQull95YgiJ55uOVbC86nejURgOSGGhjesQ4PK7ATKnVyObT+dc9rY+H+7v" +
                                   "M5uCe29VZYhq5atYphJHaqwDt11yr2uy78cIwwsl0ECzSJWYtkO14O8flPL4QXWU/ksGcMcxkBtQqq7U" +
                                   "q3ft/bRT4F0CIUSwfT5P8AHZc+g6w6/V4rUMElWb7DiOTr0J3uTpamAVBkS95mBVJAL+auZ15uYhjYSl" +
                                   "2XzPViIA6AQ0G968WxZet2YyiW909rYeHUXMc7me8gCxeIkXTP8OUIC2HPg==");
    test:assertEquals(outputs[16], "Hex encoded RSA-SHA512 signature: 2433A1A1733D998FFDB40C449AA3F3DCA0A8E550C546AB" +
                                   "F9BBEBC7A79561B8134C66FC7FFD2F5E304F1D7E1B5056743DE81CDCAC8E6701B6E30709FBD3EBD0" +
                                   "A6F13533F17239637C94D3896FA1447D9DD703F28389310DF4060493AE1EA116AC5B5E8A63A904F9" +
                                   "1E6C87683EB9B197FC884694E0664F8E3CA947D489B8C5695FC8A0E817EDAB17734612C9CC2F3FD0" +
                                   "A6854773F911355E746E17A27D6D45915271D81DFB5DA4BFC46A2E300C3935CBE2F18DD520425051" +
                                   "B03C58754D839E9EFE9A5C865A36A5883A82CACF91B8FC9A23888B8B20255D4F3273A7BBF3AB601F" +
                                   "67008F0C8385A93F32B4CF0D6D3E4D5105C94A9697FCFAA6DAB75A8DDDF3DFE52A");
}
