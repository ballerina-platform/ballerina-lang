import ballerina/io;
import ballerina/log;

public function main() {
	PackageNode? parsedFile = parseFile("inputFile.txt");
	if(parsedFile is PackageNode){
		json|error j = json.convert(parsedFile);
		    if (j is json) {
		        io:println(j);
		    }
		//io:println(parsedFile);
	}
}

//function to parse the input file, build the AST and return the PackageNode
function parseFile(string fileLocation) returns PackageNode? {
	io:ReadableCharacterChannel sourceChannel = new(io:openReadableFile(fileLocation), "UTF-8");

	BufferReader|error bReader = trap new BufferReader(capacity = 5, sourceChannel);

	if (bReader is error) {
		log:printError("error occurred while processing chars ", err = bReader);
		closeRc(sourceChannel);
		
		return null;
	}else{
		Lexer lex = new(bReader);
		ParserBufferReader pBuffer = new(lex, capacity = 5);
		Parser parser = new(pBuffer);
		PackageNode pkgNode = parser.parse();
		closeRc(sourceChannel);
		
		return pkgNode;
	}
}
function closeRc(io:ReadableCharacterChannel ch) {
	var cr = ch.close();
	if (cr is error) {
		log:printError("Error occured while closing the channel: ", err = cr);
	}
}