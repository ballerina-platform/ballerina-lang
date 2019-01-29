import ballerina/bir;

function writeSample(string path) {

    bir:Package ifSample = {
        functions: [
            {
                argsCount: 0,
                basicBlocks: [
                    {
                        id: {
                            value: "bb0"
                        },
                        instructions: [
                            new bir:ConstantLoad (
                                "CONST_LOAD",
                                new bir:VarRef (
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
                            new bir:Move (
                                "MOVE",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                            new bir:ConstantLoad (
                                "CONST_LOAD",
                                new bir:VarRef (
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
                            new bir:Move (
                                "MOVE",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                            new bir:Move (
                                "MOVE",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                            new bir:ConstantLoad (
                                "CONST_LOAD",
                                new bir:VarRef (
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
                            new bir:BinaryOp (
                                "LESS_THAN",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                        terminator: new bir:Branch (
                                        {
                                            id: {
                                                value: "bb2"
                                            }
                                        },
                                        "BRANCH",
                                        new bir:VarRef (
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
                            new bir:Move (
                                "MOVE",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                            new bir:Move (
                                "MOVE",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                            new bir:BinaryOp (
                                "ADD",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                            new bir:Move (
                                "MOVE",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                        terminator: new bir:GOTO (
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
                            new bir:Move (
                                "MOVE",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                            new bir:Move (
                                "MOVE",
                                new bir:VarRef (
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
                                new bir:VarRef (
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
                        terminator: new bir:GOTO (
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
                        terminator: new bir:Return (
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
