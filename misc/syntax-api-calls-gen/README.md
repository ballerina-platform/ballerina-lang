# The Syntax API Calls Generator

> Generate the Ballerina Syntax API calls to build the given source code.

This project aims to list the required API calls to create the given source code's syntax tree. This project will be
useful for anyone who uses the Ballerina syntax API to create source code elements.

## Generating child names JSON

This project primarily relies on [`api_gen_syntax_tree_descriptor.json`](src/main/resources/api_gen_syntax_tree_descriptor.json) file to
get the names of the children of each node type. If the Syntax API is changed, this file should be updated. Simply copy
and put this file
from [`treegen source`](../../compiler/ballerina-treegen/src/main/resources/syntax_tree_descriptor.json).
