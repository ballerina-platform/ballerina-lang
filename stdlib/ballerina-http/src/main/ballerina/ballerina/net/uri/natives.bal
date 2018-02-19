package ballerina.net.uri;

@Description {value:"Encodes the given URL"}
@Param {value:"url: URL to be encoded"}
@Param {value:"charset: Character set that URL to be encoded"}
@Return {value:"The encoded URL"}
@Return {value:"Error occured during encoding the URL"}
documentation {
Encodes the given URL.
- #url URL to be encoded.
- #charset Character set that URL to be encoded.
- #encodedUrl The encoded URL.
- #e Error occurred during encoding the URL.
}
public native function encode (string url, string charset) (string encodedUrl, error e);

@Description {value:"Decodes the given URL"}
@Param {value:"url: URL to be decoded"}
@Param {value:"charset: Character set that URL to be decoded"}
@Return {value:"The decoded URL"}
@Return {value:"Error occured during decoding the URL"}
documentation {
Decodes the given URL.
- #url URL to be decoded.
- #charset Character set that URL to be decoded.
- #decodedUrl The encoded URL.
- #e Error occurred during decoding the URL.
}
public native function decode (string url, string charset) (string decodedUrl, error e);