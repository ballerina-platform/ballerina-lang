import ballerina/io;

function writeSample(string path) {

    BIRPackage ifSample = {
        functions: [
            {
                argsCount: 0,
                basicBlocks: [
                    {
                        id: {
                            value: "bb0"
                        },
                        instructions: [
                            new ConstantLoad (
                                "CONST_LOAD",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%2"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                "int",
                                1
                            ),
                            new Move (
                                "MOVE",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%1"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%2"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new ConstantLoad (
                                "CONST_LOAD",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%4"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                "int",
                                0
                            ),
                            new Move (
                                "MOVE",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%3"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%4"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new Move (
                                "MOVE",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%5"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%1"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new ConstantLoad (
                                "CONST_LOAD",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%6"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                "int",
                                10
                            ),
                            new BinaryOp (
                                LESS_THAN,
                                new BIRVarRef (
                                    "VAR_REF",
                                    "boolean",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%7"
                                        },
                                        typeValue: "boolean"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%5"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%6"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                "boolean"
                            )
                        ],
                        terminator: new Branch (
                                        {
                                            id: {
                                                value: "bb2"
                                            }
                                        },
                                        "BRANCH",
                                        new BIRVarRef (
                                            "VAR_REF",
                                            "boolean",
                                            {
                                                kind: "TEMP",
                                                name: {
                                                    value: "%7"
                                                },
                                                typeValue: "boolean"
                                            }
                                        ),
                                        {
                                            id: {
                                                value: "bb1"
                                            }
                                        }
                        )
                    },
                    {
                        id: {
                            value: "bb1"
                        },
                        instructions: [
                            new Move (
                                "MOVE",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%8"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%3"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new Move (
                                "MOVE",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%9"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%1"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new BinaryOp (
                                ADD,
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%10"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%8"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%9"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                "int"
                            ),
                            new Move (
                                "MOVE",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%3"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%10"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            )
                        ],
                        terminator: new GOTO (
                                        "GOTO",
                                        {
                                            id: {
                                                value: "bb2"
                                            }
                                        }
                        )
                    },
                    {
                        id: {
                            value: "bb2"
                        },
                        instructions: [
                            new Move (
                                "MOVE",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%11"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%3"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new Move (
                                "MOVE",
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "RETURN",
                                        name: {
                                            value: "%0"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%11"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            )
                        ],
                        terminator: new GOTO (
                                        "GOTO",
                                        {
                                            id: {
                                                value: "bb3"
                                            }
                                        }
                        )
                    },
                    {
                        id: {
                            value: "bb3"
                        },
                        instructions: [
                        ],
                        terminator: new Return (
                                        "RETURN"
                        )
                    }
                ],
                isDeclaration: false,
                localVars: [
                    {
                        kind: "RETURN",
                        name: {
                            value: "%0"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "LOCAL",
                        name: {
                            value: "%1"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%2"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "LOCAL",
                        name: {
                            value: "%3"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%4"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%5"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%6"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%7"
                        },
                        typeValue: "boolean"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%8"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%9"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%10"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%11"
                        },
                        typeValue: "int"
                    }
                ],
                name: {
                    value: "main"
                },
                typeValue: {
                    paramTypes: [
                    ],
                    retType: "int",
                    tag: 12
                },
                visibility: "PRIVATE"
            }
        ],
        name: {
            value: "."
        },
        org: {
            value: "$anon"
        },
        types: [
        ],
        versionValue: {
            value: "0.0.0"
        }
    };
    genPackage(ifSample, path, false);
}
