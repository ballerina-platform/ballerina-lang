
@Description {value:"Encodes the given URL"}
@Param {value:"url: URL to be encoded"}
@Param {value:"charset: Charactor set that URL to be encoded"}
@Return {value:"The encoded URL"}
@Return {value:"Error occured during encoding the URL"}
public native function encode (string url, string charset) returns (string | error);

@Description {value:"Decodes the given URL"}
@Param {value:"url: URL to be decoded"}
@Param {value:"charset: Charactor set that URL to be decoded"}
@Return {value:"The decoded URL"}
@Return {value:"Error occured during decoding the URL"}
public native function decode (string url, string charset) returns (string | error);