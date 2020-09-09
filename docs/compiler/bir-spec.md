# Ballerina Intermediate Representation (BIR) Specification

## BIR usage in compiler

```
                                                                                                           ,-> JVM Code gen
Lexer/Parser -> Type Check -> Semantic analyzer -> Desugar -> BIR gen -> BIR instrumentation/optimization -
                                                                                                           `-> LLVM Code gen
```

Above `BIR gen` phase outputs the BIR format that will be used by the later phases.
By default BIR is represented as an internal object model, via set of sub classes of
`org.wso2.ballerinalang.compiler.bir.model.BIRNode` class and `org.wso2.ballerinalang.compiler.bir.model.BIRInstruction`
class.

You may view this in text format by passing `--dump-bir` flag to the `ballerina build` command or save it to a binary
file by passing `--dump-bir-file /location/to/save.bir`

## Goals of BIR

1. Support multiple backend implementations.

2. Help ballerina compiler writes as a diagnostic tool.

3. Act as the internal format for the optimizer and instrumentation.


## Bir binary file structure

Binary file constant of two main areas, Constant Pool and the Module section.

WIP
- Issue [ballerina-lang/issues/10257](https://github.com/ballerina-platform/ballerina-lang/issues/10257)



