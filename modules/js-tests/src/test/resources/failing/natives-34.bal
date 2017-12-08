package ballerina.net.uri;

@Description { value:"Encodes the given URL"}
@Param { value:"url: URL to be encoded" }
@Return { value:"string: The encoded url" }
public native function encode (string url) (string);

