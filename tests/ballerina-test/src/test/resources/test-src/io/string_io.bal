import ballerina/io;

function getJson(string content,string encoding) returns json{
    io:CharacterChannel channel = io:newStringRef(content,encoding = encoding);
    json result =check channel.readJson();
    var closeResult = channel.close();
    return result;
}
