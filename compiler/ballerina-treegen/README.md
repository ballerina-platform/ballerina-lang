# The _treegen_ tool
This tool generates source code required to work with Ballerina syntax trees. By default, it generates the following classes.

- [Internal syntax tree nodes](../ballerina-parser/src/main/java/io/ballerinalang/compiler/internal/parser/tree/)
- [External syntax tree nodes](../ballerina-parser/src/main/java/io/ballerinalang/compiler/syntax/tree/)
- [Internal node factory class](../ballerina-parser/src/main/java/io/ballerinalang/compiler/internal/parser/tree/STNodeFactory.java)
- [External node factory class](../ballerina-parser/src/main/java/io/ballerinalang/compiler/syntax/tree/NodeFactory.java)
- [NodeVisitor](../ballerina-parser/src/main/java/io/ballerinalang/compiler/syntax/tree/NodeVisitor.java)
- [TreeModifier](../ballerina-parser/src/main/java/io/ballerinalang/compiler/syntax/tree/TreeModifier.java)
- [NodeTransformer](../ballerina-parser/src/main/java/io/ballerinalang/compiler/syntax/tree/NodeTransformer.java)

## How to use 
### Step 1:
Update the [`syntax_tree_descriptor.json`](src/main/resources/syntax_tree_descriptor.json) with the necessary changes. 

### Step 2:
Run the following command. By default, the tool does an in-place update of the above-mentioned classes.
```bash
./gradle treegen
```

The generated classes are available in output directories specified by the properties `internal.node.output.dir` and `external.node.output.dir` in the [`treegen_config.properties`](src/main/resources/treegen_config.properties) file. 

## How to generate custom code
You can extend this tool to generate custom sources of your choice by providing an implementation of the [`Target`](src/main/java/io/ballerinalang/compiler/internal/treegen/targets/Target.java) class.