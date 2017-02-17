/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define(['require', 'jquery', 'lodash'], function (require, $, _) {
    var environment_content = {};

    /**
     * Get the packages from the package list api.
     * */
    environment_content.getPackages = function () {
        //$.ajax({
        //    type: "GET",
        //    url: requirejs.s.contexts._.config.package_listing,
        //    contentType: "application/json; charset=utf-8",
        //    async: false,
        //    dataType: "json",
        //    success: function (response) {
        //        data = response;
        //    },
        //    error: function (xhr, textStatus, errorThrown) {
        //        data = {"error": true, "message": "Unable to retrieve packages."};
        //    }
        //});
        var data = [
            {
                "name": "ballerina.lang.string",
                "connectors": [],
                "functions": [
                    {
                        "name": "valueOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "float",
                                "type": "float"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "replaceFirst",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "valueOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "long",
                                "type": "long"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "replaceAll",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "hasSuffix",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ]
                    },
                    {
                        "name": "toUpperCase",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "replace",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "contains",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ]
                    },
                    {
                        "name": "valueOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "valueOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "valueOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "unescape",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "valueOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "trim",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "valueOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "valueOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "double",
                                "type": "double"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "lastIndexOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "subString",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "equalsIgnoreCase",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ]
                    },
                    {
                        "name": "toLowerCase",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "indexOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "hasPrefix",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.lang.message",
                "connectors": [],
                "functions": [
                    {
                        "name": "addHeader",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getJsonPayload",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ]
                    },
                    {
                        "name": "removeHeader",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "clone",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ]
                    },
                    {
                        "name": "setHeader",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getHeader",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "getHeaders",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string[]",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "setXmlPayload",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getStringPayload",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "setStringPayload",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "setJsonPayload",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getXmlPayload",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ]
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.net.uri",
                "connectors": [],
                "functions": [
                    {
                        "name": "getQueryParam",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "encode",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.lang.map",
                "connectors": [],
                "functions": [
                    {
                        "name": "remove",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "map",
                                "type": "map"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "keys",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string[]",
                                "type": "string"
                            }
                        ]
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.lang.xml",
                "connectors": [],
                "functions": [
                    {
                        "name": "toString",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "getXml",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ]
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "addElement",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "remove",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getString",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "addAttribute",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "addAttribute",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getXml",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ]
                    },
                    {
                        "name": "getString",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "remove",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "map",
                                "type": "map"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "addElement",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ],
                        "returnParams": []
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.lang.array",
                "connectors": [],
                "functions": [
                    {
                        "name": "copyOfRange",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "long[]",
                                "type": "long"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "long[]",
                                "type": "long"
                            }
                        ]
                    },
                    {
                        "name": "copyOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message[]",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "message[]",
                                "type": "message"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "long[]",
                                "type": "long"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string[]",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "copyOfRange",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message[]",
                                "type": "message"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "message[]",
                                "type": "message"
                            }
                        ]
                    },
                    {
                        "name": "copyOfRange",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json[]",
                                "type": "json"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "json[]",
                                "type": "json"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "float[]",
                                "type": "float"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml[]",
                                "type": "xml"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "copyOfRange",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string[]",
                                "type": "string"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string[]",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "copyOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int[]",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int[]",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "copyOfRange",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "double[]",
                                "type": "double"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "double[]",
                                "type": "double"
                            }
                        ]
                    },
                    {
                        "name": "copyOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json[]",
                                "type": "json"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "json[]",
                                "type": "json"
                            }
                        ]
                    },
                    {
                        "name": "copyOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "double[]",
                                "type": "double"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "double[]",
                                "type": "double"
                            }
                        ]
                    },
                    {
                        "name": "copyOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "long[]",
                                "type": "long"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "long[]",
                                "type": "long"
                            }
                        ]
                    },
                    {
                        "name": "copyOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string[]",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string[]",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "copyOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "float[]",
                                "type": "float"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "float[]",
                                "type": "float"
                            }
                        ]
                    },
                    {
                        "name": "sort",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string[]",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string[]",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "copyOf",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml[]",
                                "type": "xml"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "xml[]",
                                "type": "xml"
                            }
                        ]
                    },
                    {
                        "name": "copyOfRange",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "float[]",
                                "type": "float"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "float[]",
                                "type": "float"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "double[]",
                                "type": "double"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "copyOfRange",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int[]",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int[]",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "copyOfRange",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml[]",
                                "type": "xml"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "xml[]",
                                "type": "xml"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json[]",
                                "type": "json"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message[]",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "length",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int[]",
                                "type": "int"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.lang.json",
                "connectors": [],
                "functions": [
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "double",
                                "type": "double"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "double",
                                "type": "double"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "toString",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "remove",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getFloat",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "float",
                                "type": "float"
                            }
                        ]
                    },
                    {
                        "name": "getDouble",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "double",
                                "type": "double"
                            }
                        ]
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "double",
                                "type": "double"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getBoolean",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ]
                    },
                    {
                        "name": "getJson",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ]
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "float",
                                "type": "float"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getString",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "rename",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "float",
                                "type": "float"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "float",
                                "type": "float"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getInt",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "add",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.util",
                "connectors": [],
                "functions": [
                    {
                        "name": "getHmac",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "base64encode",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "base64decode",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "getRandomString",
                        "annotations": [],
                        "parameters": [],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.net.http",
                "connectors": [
                    {
                        "name": "ClientConnector",
                        "annotations": [],
                        "returnParameters": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "actions": [
                            {
                                "name": "execute",
                                "annotations": [],
                                "parameters": [
                                    {
                                        "name": "ClientConnector",
                                        "type": "ClientConnector"
                                    },
                                    {
                                        "name": "string",
                                        "type": "string"
                                    },
                                    {
                                        "name": "string",
                                        "type": "string"
                                    },
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ],
                                "returnParams": [
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ]
                            },
                            {
                                "name": "head",
                                "annotations": [],
                                "parameters": [
                                    {
                                        "name": "ClientConnector",
                                        "type": "ClientConnector"
                                    },
                                    {
                                        "name": "string",
                                        "type": "string"
                                    },
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ],
                                "returnParams": [
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ]
                            },
                            {
                                "name": "delete",
                                "annotations": [],
                                "parameters": [
                                    {
                                        "name": "ClientConnector",
                                        "type": "ClientConnector"
                                    },
                                    {
                                        "name": "string",
                                        "type": "string"
                                    },
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ],
                                "returnParams": [
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ]
                            },
                            {
                                "name": "put",
                                "annotations": [],
                                "parameters": [
                                    {
                                        "name": "ClientConnector",
                                        "type": "ClientConnector"
                                    },
                                    {
                                        "name": "string",
                                        "type": "string"
                                    },
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ],
                                "returnParams": [
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ]
                            },
                            {
                                "name": "post",
                                "annotations": [],
                                "parameters": [
                                    {
                                        "name": "ClientConnector",
                                        "type": "ClientConnector"
                                    },
                                    {
                                        "name": "string",
                                        "type": "string"
                                    },
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ],
                                "returnParams": [
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ]
                            },
                            {
                                "name": "patch",
                                "annotations": [],
                                "parameters": [
                                    {
                                        "name": "ClientConnector",
                                        "type": "ClientConnector"
                                    },
                                    {
                                        "name": "string",
                                        "type": "string"
                                    },
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ],
                                "returnParams": [
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ]
                            },
                            {
                                "name": "get",
                                "annotations": [],
                                "parameters": [
                                    {
                                        "name": "ClientConnector",
                                        "type": "ClientConnector"
                                    },
                                    {
                                        "name": "string",
                                        "type": "string"
                                    },
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ],
                                "returnParams": [
                                    {
                                        "name": "message",
                                        "type": "message"
                                    }
                                ]
                            }
                        ]
                    }
                ],
                "functions": [
                    {
                        "name": "getStatusCode",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "setContentLength",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getMethod",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "setReasonPhrase",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "setStatusCode",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getContentLength",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ]
                    },
                    {
                        "name": "convertToResponse",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "message",
                                "type": "message"
                            }
                        ],
                        "returnParams": []
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.lang.exceptions",
                "connectors": [],
                "functions": [
                    {
                        "name": "getStackTrace",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "exception",
                                "type": "exception"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "getCategory",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "exception",
                                "type": "exception"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "setCause",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "exception",
                                "type": "exception"
                            },
                            {
                                "name": "exception",
                                "type": "exception"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "setMessage",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "exception",
                                "type": "exception"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "set",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "exception",
                                "type": "exception"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "getMessage",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "exception",
                                "type": "exception"
                            }
                        ],
                        "returnParams": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ]
                    },
                    {
                        "name": "setCategory",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "exception",
                                "type": "exception"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    }
                ],
                "structs": []
            },
            {
                "name": "ballerina.lang.system",
                "connectors": [],
                "functions": [
                    {
                        "name": "currentTimeMillis",
                        "annotations": [],
                        "parameters": [],
                        "returnParams": [
                            {
                                "name": "long",
                                "type": "long"
                            }
                        ]
                    },
                    {
                        "name": "print",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "long",
                                "type": "long"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "log",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "epochTime",
                        "annotations": [],
                        "parameters": [],
                        "returnParams": [
                            {
                                "name": "long",
                                "type": "long"
                            }
                        ]
                    },
                    {
                        "name": "println",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "double",
                                "type": "double"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "println",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "println",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "long",
                                "type": "long"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "print",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "print",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "double",
                                "type": "double"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "println",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "print",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "float",
                                "type": "float"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "println",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "print",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "log",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "long",
                                "type": "long"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "log",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "log",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "double",
                                "type": "double"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "nanoTime",
                        "annotations": [],
                        "parameters": [],
                        "returnParams": [
                            {
                                "name": "long",
                                "type": "long"
                            }
                        ]
                    },
                    {
                        "name": "println",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "print",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "string",
                                "type": "string"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "log",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "boolean",
                                "type": "boolean"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "println",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "print",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "json",
                                "type": "json"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "log",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "int",
                                "type": "int"
                            },
                            {
                                "name": "float",
                                "type": "float"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "print",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "xml",
                                "type": "xml"
                            }
                        ],
                        "returnParams": []
                    },
                    {
                        "name": "println",
                        "annotations": [],
                        "parameters": [
                            {
                                "name": "float",
                                "type": "float"
                            }
                        ],
                        "returnParams": []
                    }
                ],
                "structs": []
            }
        ];
        return data;
    };
    return environment_content;
});