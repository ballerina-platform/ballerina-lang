import ballerina/io;

function main(string... args) {
    BIRPackage whileSample = {
        functions: [
            {
                argsCount: 0,
                basicBlocks: [
                    {
                        id: {
                            value: "bb0"
                        },
                        instructions: [
                            new Move (
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
                                new BIRConstant (
                                    "CONST",
                                    "int",
                                    1
                                )
                            ),
                            new Move (
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%2"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRConstant (
                                    "CONST",
                                    "int",
                                    0
                                )
                            )
                        ],
                        terminator: new GOTO (
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
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
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
                                        kind: "LOCAL",
                                        name: {
                                            value: "%1"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new BinaryOp (
                                LESS_THAN,
                                (),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "boolean",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%4"
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
                                            value: "%3"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRConstant (
                                    "CONST",
                                    "int",
                                    10
                                ),
                                "boolean"
                            )
                        ],
                        terminator: new Branch (
                                        {
                                            id: {
                                                value: "bb3"
                                            }
                                        },
                                        new BIRVarRef (
                                            "VAR_REF",
                                            "boolean",
                                            {
                                                kind: "TEMP",
                                                name: {
                                                    value: "%4"
                                                },
                                                typeValue: "boolean"
                                            }
                                        ),
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
                                            value: "%2"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new Move (
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
                                (),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%7"
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
                                "int"
                            ),
                            new Move (
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%2"
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
                                            value: "%7"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new Move (
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
                                            value: "%1"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new BinaryOp (
                                ADD,
                                (),
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
                                        kind: "TEMP",
                                        name: {
                                            value: "%8"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRConstant (
                                    "CONST",
                                    "int",
                                    1
                                ),
                                "int"
                            ),
                            new Move (
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
                                            value: "%9"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            )
                        ],
                        terminator: new GOTO (
                                        {
                                            id: {
                                                value: "bb1"
                                            }
                                        }
                        )
                    },
                    {
                        id: {
                            value: "bb3"
                        },
                        instructions: [
                            new Move (
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
                                        kind: "LOCAL",
                                        name: {
                                            value: "%1"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new Move (
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
                                            value: "%10"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            )
                        ],
                        terminator: new GOTO (
                                        {
                                            id: {
                                                value: "bb4"
                                            }
                                        }
                        )
                    },
                    {
                        id: {
                            value: "bb4"
                        },
                        instructions: [
                        ],
                        terminator: new Return ()
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
                        kind: "LOCAL",
                        name: {
                            value: "%2"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
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
                        typeValue: "boolean"
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
                        typeValue: "int"
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
        types: [
        ],
        versionValue: {
            value: "0.0.0"
        }
    };

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
                            new Move (
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
                                new BIRConstant (
                                    "CONST",
                                    "int",
                                    1
                                )
                            ),
                            new Move (
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%2"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRConstant (
                                    "CONST",
                                    "int",
                                    0
                                )
                            ),
                            new Move (
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
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
                                        kind: "LOCAL",
                                        name: {
                                            value: "%1"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new BinaryOp (
                                LESS_THAN,
                                (),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "boolean",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%4"
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
                                            value: "%3"
                                        },
                                        typeValue: "int"
                                    }
                                ),
                                new BIRConstant (
                                    "CONST",
                                    "int",
                                    10
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
                                        new BIRVarRef (
                                            "VAR_REF",
                                            "boolean",
                                            {
                                                kind: "TEMP",
                                                name: {
                                                    value: "%4"
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
                                            value: "%2"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new Move (
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
                                (),
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "TEMP",
                                        name: {
                                            value: "%7"
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
                                "int"
                            ),
                            new Move (
                                new BIRVarRef (
                                    "VAR_REF",
                                    "int",
                                    {
                                        kind: "LOCAL",
                                        name: {
                                            value: "%2"
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
                                            value: "%7"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            )
                        ],
                        terminator: new GOTO (
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
                                            value: "%2"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            ),
                            new Move (
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
                                            value: "%8"
                                        },
                                        typeValue: "int"
                                    }
                                )
                            )
                        ],
                        terminator: new GOTO (
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
                        kind: "LOCAL",
                        name: {
                            value: "%2"
                        },
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
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
                        typeValue: "boolean"
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
                        typeValue: "int"
                    },
                    {
                        kind: "TEMP",
                        name: {
                            value: "%8"
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
        types: [
        ],
        versionValue: {
            value: "0.0.0"
        }
    };
    genPackage(ifSample, args[0]);
}
