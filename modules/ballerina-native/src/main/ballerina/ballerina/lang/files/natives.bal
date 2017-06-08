package ballerina.lang.files;

import ballerina.doc;


@doc:Description { value:"This function archives a file to a given location"}
@doc:Param { value:"target: File/Directory that should be archived" }
@doc:Param { value:"destination: The location where the Archived file should be written" }
native function archive (file target, file destination);

@doc:Description { value:"This function copies a file from a given location to another"}
@doc:Param { value:"target: File/Directory that should be copied" }
@doc:Param { value:"destination: The location where the File/Directory should be pasted" }
native function copy (file target, file destination);

@doc:Description { value:"This function deletes a file from a given location"}
@doc:Param { value:"target: File/Directory that should be copied" }
native function delete (file target);

@doc:Description { value:"Gets the file from path string"}
@doc:Param { value:"path: A string value representing the path of the file" }
@doc:Return { value:"file: The BFile object representing file in path" }
native function getFileFromPath (string path) (file);

@doc:Description { value:"Gets the inputstream from file"}
@doc:Param { value:"file: The BFile reference" }
@doc:Return { value:"is: The inputstream of file" }
native function getInputStream (file file) (inputstream);

@doc:Description { value:"Gets the outputstream from file"}
@doc:Param { value:"file: The BFile reference" }
@doc:Return { value:"is: The outputstream of file" }
native function getOutputStream (file file, boolean append) (outputstream);

@doc:Description { value:"This function moves a file from a given location to another"}
@doc:Param { value:"target: File/Directory that should be copied" }
@doc:Param { value:"destination: The location where the File/Directory should be pasted" }
native function move (file target, file destination);
