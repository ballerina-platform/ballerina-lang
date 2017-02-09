var NELListener = require('../generated-parser/NELListener').NELListener;

var count = 0;
var currentResource;
var rootNode;
var source;
var service;
var parentStack = new Array();
var lastMediatorCallVariable = "";
var currentMediator;
var setHeader = false;

function TreeNode(value, type, cStart, cEnd, parameters) {
    //parameters is an array of parameters
    this.object = undefined;
    this.children = [];
    this.value = value;
    this.type = type;
    this.configStart = cStart;
    this.configEnd = cEnd;
    this.parameters = parameters;

    this.getChildren = function () {
        return this.children;
    };

    this.getValue = function () {
        return this.value;
    };

    this.setConfigStart = function (cStart) {
        this.configStart = cStart;
    };

    this.setConfigEnd = function (cEnd) {
        this.configEnd = cEnd;
    };

    this.appendConfigStart = function (value) {
        if (!this.configStart) {
            this.configStart = "";
        }
        this.configStart += value;
    };

    this.setParameters = function (parameters) {
        this.parameters = parameters;
    };
    this.appendParameter = function (parameter) {
        //parameter is an object eg: {key: x, value: y}
        if (this.parameters) {
            this.parameters[this.parameters.length] = parameter;
        } else {
            this.parameters = [parameter];
        }
    };
    this.setParameterValue = function (key, value) {
        this.parameters.forEach(function (parameter, index) {
            if (parameter[index].key === key) {
                parameter[index].value = parameter[index].value.append(value);
            }
        })
    };
    this.getParameterValue = function (key) {
        var value;
        this.parameters.forEach(function (parameter) {
            if (parameter.key === key) {
                value = parameter.value;
            }
        });
        return value;
    }
}

NELListenerImpl = function () {
    NELListener.call(this); // inherit default listener
    return this;
};

// inherit default listener
NELListenerImpl.prototype = Object.create(NELListener.prototype);
NELListenerImpl.prototype.constructor = NELListenerImpl;

// Enter a parse tree produced by NELParser#sourceFile.
NELListenerImpl.prototype.enterSourceFile = function (ctx) {
    //TODO set source parameters
    rootNode = new TreeNode("Service", "Service");
};

// Exit a parse tree produced by NELParser#sourceFile.
NELListenerImpl.prototype.exitSourceFile = function (ctx) {
    rootNode.setConfigEnd("\n");
    generatedTree = JSON.stringify(rootNode);
    return rootNode;
};


// Enter a parse tree produced by NELParser#definition.
NELListenerImpl.prototype.enterDefinition = function (ctx) {
};

// Exit a parse tree produced by NELParser#definition.
NELListenerImpl.prototype.exitDefinition = function (ctx) {
};


// Enter a parse tree produced by NELParser#constants.
NELListenerImpl.prototype.enterConstants = function (ctx) {
};

// Exit a parse tree produced by NELParser#constants.
NELListenerImpl.prototype.exitConstants = function (ctx) {
};


// Enter a parse tree produced by NELParser#globalVariables.
NELListenerImpl.prototype.enterGlobalVariables = function (ctx) {
};

// Exit a parse tree produced by NELParser#globalVariables.
NELListenerImpl.prototype.exitGlobalVariables = function (ctx) {
};


// Enter a parse tree produced by NELParser#resources.
NELListenerImpl.prototype.enterResources = function (ctx) {
};

// Exit a parse tree produced by NELParser#resources.
NELListenerImpl.prototype.exitResources = function (ctx) {
};


// Enter a parse tree produced by NELParser#packageDef.
NELListenerImpl.prototype.enterPackageDef = function (ctx) {
    rootNode.appendParameter({
                                 key: "packageDefinition",
                                 value: ctx.qualifiedName().getText()
                             });
    rootNode.appendConfigStart(ctx.getText() + "\n");
};

// Exit a parse tree produced by NELParser#packageDef.
NELListenerImpl.prototype.exitPackageDef = function (ctx) {
};


// Enter a parse tree produced by NELParser#path.
NELListenerImpl.prototype.enterPath = function (ctx) {
    rootNode.appendParameter({
                                 key: "path",
                                 value: ctx.StringLiteral().getText().replace(/['"]+/g, '')
                             });
    rootNode.appendConfigStart(ctx.getText() + "\n");
};

// Exit a parse tree produced by NELParser#path.
NELListenerImpl.prototype.exitPath = function (ctx) {
};


// Enter a parse tree produced by NELParser#source.
NELListenerImpl.prototype.enterSource = function (ctx) {
    source = {};
    rootNode.appendConfigStart(ctx.getText() + "\n");
};

// Exit a parse tree produced by NELParser#source.
NELListenerImpl.prototype.exitSource = function (ctx) {
    rootNode.appendParameter({
                                 key: "sourceValues",
                                 value: source
                             });
};

// Enter a parse tree produced by NELParser#api.
NELListenerImpl.prototype.enterApi = function (ctx) {
    service = {};
    rootNode.appendConfigStart(ctx.getText() + "\n");
};

// Exit a parse tree produced by NELParser#api.
NELListenerImpl.prototype.exitApi = function (ctx) {
};


// Enter a parse tree produced by NELParser#resourcePath.
NELListenerImpl.prototype.enterResourcePath = function (ctx) {
    currentResource.appendConfigStart(ctx.getText() + '\n');
    currentResource.appendParameter({key: "path", value: ctx.StringLiteral().getText().replace(/['"]+/g, '')});
};

// Exit a parse tree produced by NELParser#resourcePath.
NELListenerImpl.prototype.exitResourcePath = function (ctx) {
};


// Enter a parse tree produced by NELParser#getMethod.
NELListenerImpl.prototype.enterGetMethod = function (ctx) {
    currentResource.appendParameter({key: "get", value: true});
    currentResource.appendConfigStart(ctx.getText() + '\n');
};

// Exit a parse tree produced by NELParser#getMethod.
NELListenerImpl.prototype.exitGetMethod = function (ctx) {
};


// Enter a parse tree produced by NELParser#postMethod.
NELListenerImpl.prototype.enterPostMethod = function (ctx) {
    currentResource.appendParameter({key: "post", value: true});
    currentResource.appendConfigStart(ctx.getText() + '\n');
};

// Exit a parse tree produced by NELParser#postMethod.
NELListener.prototype.exitPostMethod = function (ctx) {
};


// Enter a parse tree produced by NELParser#putMethod.
NELListenerImpl.prototype.enterPutMethod = function (ctx) {
    currentResource.appendParameter({key: "put", value: true});
    currentResource.appendConfigStart(ctx.getText() + '\n');
};

// Exit a parse tree produced by NELParser#putMethod.
NELListenerImpl.prototype.exitPutMethod = function (ctx) {
};


// Enter a parse tree produced by NELParser#deleteMethod.
NELListenerImpl.prototype.enterDeleteMethod = function (ctx) {
    currentResource.appendParameter({key: "delete", value: true});
    currentResource.appendConfigStart(ctx.getText() + '\n');
};

// Exit a parse tree produced by NELParser#deleteMethod.
NELListenerImpl.prototype.exitDeleteMethod = function (ctx) {
};


// Enter a parse tree produced by NELParser#headMethod.
NELListenerImpl.prototype.enterHeadMethod = function (ctx) {
};

// Exit a parse tree produced by NELParser#headMethod.
NELListenerImpl.prototype.exitHeadMethod = function (ctx) {
};


// Enter a parse tree produced by NELParser#prodAnt.
NELListenerImpl.prototype.enterProdAnt = function (ctx) {
};

// Exit a parse tree produced by NELParser#prodAnt.
NELListenerImpl.prototype.exitProdAnt = function (ctx) {
};


// Enter a parse tree produced by NELParser#conAnt.
NELListenerImpl.prototype.enterConAnt = function (ctx) {
};

// Exit a parse tree produced by NELParser#conAnt.
NELListenerImpl.prototype.exitConAnt = function (ctx) {
};


// Enter a parse tree produced by NELParser#antApiOperation.
NELListenerImpl.prototype.enterAntApiOperation = function (ctx) {
};

// Exit a parse tree produced by NELParser#antApiOperation.
NELListenerImpl.prototype.exitAntApiOperation = function (ctx) {
};


// Enter a parse tree produced by NELParser#antApiResponses.
NELListenerImpl.prototype.enterAntApiResponses = function (ctx) {
};

// Exit a parse tree produced by NELParser#antApiResponses.
NELListenerImpl.prototype.exitAntApiResponses = function (ctx) {
};


// Enter a parse tree produced by NELParser#antApiResponseSet.
NELListenerImpl.prototype.enterAntApiResponseSet = function (ctx) {
};

// Exit a parse tree produced by NELParser#antApiResponseSet.
NELListenerImpl.prototype.exitAntApiResponseSet = function (ctx) {
};


// Enter a parse tree produced by NELParser#antApiResponse.
NELListenerImpl.prototype.enterAntApiResponse = function (ctx) {
};

// Exit a parse tree produced by NELParser#antApiResponse.
NELListenerImpl.prototype.exitAntApiResponse = function (ctx) {
};


// Enter a parse tree produced by NELParser#elementValuePairs.
NELListenerImpl.prototype.enterElementValuePairs = function (ctx) {
};

// Exit a parse tree produced by NELParser#elementValuePairs.
NELListenerImpl.prototype.exitElementValuePairs = function (ctx) {
};


// Enter a parse tree produced by NELParser#sourceElementValuePairs.
NELListenerImpl.prototype.enterSourceElementValuePairs = function (ctx) {
    source = {};
};

// Exit a parse tree produced by NELParser#sourceElementValuePairs.
NELListenerImpl.prototype.exitSourceElementValuePairs = function (ctx) {
};


// Enter a parse tree produced by NELParser#interfaceDeclaration.
NELListenerImpl.prototype.enterInterfaceDeclaration = function (ctx) {
    source.interface = ctx.StringLiteral().getText().replace(/['"]+/g, '');
};

// Exit a parse tree produced by NELParser#interfaceDeclaration.
NELListenerImpl.prototype.exitInterfaceDeclaration = function (ctx) {
};


// Enter a parse tree produced by NELParser#apiElementValuePairs.
NELListenerImpl.prototype.enterApiElementValuePairs = function (ctx) {
};

// Exit a parse tree produced by NELParser#apiElementValuePairs.
NELListenerImpl.prototype.exitApiElementValuePairs = function (ctx) {
    rootNode.appendParameter({key: "serviceValues", value: service})
};


// Enter a parse tree produced by NELParser#protocol.
NELListenerImpl.prototype.enterProtocol = function (ctx) {
};

// Exit a parse tree produced by NELParser#protocol.
NELListenerImpl.prototype.exitProtocol = function (ctx) {
};


// Enter a parse tree produced by NELParser#host.
NELListenerImpl.prototype.enterHost = function (ctx) {
};

// Exit a parse tree produced by NELParser#host.
NELListenerImpl.prototype.exitHost = function (ctx) {
};


// Enter a parse tree produced by NELParser#port.
NELListenerImpl.prototype.enterPort = function (ctx) {
};

// Exit a parse tree produced by NELParser#port.
NELListenerImpl.prototype.exitPort = function (ctx) {
};


// Enter a parse tree produced by NELParser#tags.
NELListenerImpl.prototype.enterTags = function (ctx) {
    service.tags = [];
};

// Exit a parse tree produced by NELParser#tags.
NELListenerImpl.prototype.exitTags = function (ctx) {
};


// Enter a parse tree produced by NELParser#tag.
NELListenerImpl.prototype.enterTag = function (ctx) {
    var tags = ctx.StringLiteral();
    tags.forEach(function (t, index) {
        var tag = {tagValue: t.getText().replace(/['"]+/g, '')};
        service.tags.push(t.getText().replace(/['"]+/g, ''));
    });
};

// Exit a parse tree produced by NELParser#tag.
NELListenerImpl.prototype.exitTag = function (ctx) {
};


// Enter a parse tree produced by NELParser#descripton.
NELListenerImpl.prototype.enterDescripton = function (ctx) {
    service.description = ctx.StringLiteral().getText().replace(/['"]+/g, '');
};

// Exit a parse tree produced by NELParser#descripton.
NELListenerImpl.prototype.exitDescripton = function (ctx) {
};


// Enter a parse tree produced by NELParser#producer.
NELListenerImpl.prototype.enterProducer = function (ctx) {
    service.producer = {};
};

// Exit a parse tree produced by NELParser#producer.
NELListenerImpl.prototype.exitProducer = function (ctx) {
};


// Enter a parse tree produced by NELParser#constant.
NELListenerImpl.prototype.enterConstant = function (ctx) {
    var type = (ctx.classType() != null) ? ctx.classType().getText() : ctx.type().getText();
    var endpoint = {};
    var uri, name;
    if (type === "endpoint") {
        endpoint.uri = ctx.StringLiteral().getText().replace(/['"]+/g, '');
        uri = ctx.StringLiteral().getText().replace(/['"]+/g, '');
        endpoint.name = ctx.Identifier()[0].getText();
        name = ctx.Identifier()[0].getText();
    }
    var endpointNode = new TreeNode("Endpoint", "Endpoint", "constant endpoint " + name + " = new EndPoint(" + uri
                                                            + ")", ";\n",
        [{
            key: "title",
            value: name
        },
            {
                key: "url",
                value: uri
            }]);
    rootNode.getChildren().push(endpointNode);
};

// Exit a parse tree produced by NELParser#constant.
NELListenerImpl.prototype.exitConstant = function (ctx) {
};


// Enter a parse tree produced by NELParser#globalVariable.
NELListenerImpl.prototype.enterGlobalVariable = function (ctx) {
};

// Exit a parse tree produced by NELParser#globalVariable.
NELListenerImpl.prototype.exitGlobalVariable = function (ctx) {
};


// Enter a parse tree produced by NELParser#elementValuePair.
NELListenerImpl.prototype.enterElementValuePair = function (ctx) {
};

// Exit a parse tree produced by NELParser#elementValuePair.
NELListenerImpl.prototype.exitElementValuePair = function (ctx) {
};


// Enter a parse tree produced by NELParser#elementValue.
NELListenerImpl.prototype.enterElementValue = function (ctx) {
};

// Exit a parse tree produced by NELParser#elementValue.
NELListenerImpl.prototype.exitElementValue = function (ctx) {
};


// Enter a parse tree produced by NELParser#resource.
NELListenerImpl.prototype.enterResource = function (ctx) {
    currentResource = new TreeNode("Resource", "Resource");
};

// Exit a parse tree produced by NELParser#resource.
NELListenerImpl.prototype.exitResource = function (ctx) {
    currentResource.setConfigEnd("}");
    rootNode.getChildren().push(currentResource);
};


// Enter a parse tree produced by NELParser#httpMethods.
NELListenerImpl.prototype.enterHttpMethods = function (ctx) {
};

// Exit a parse tree produced by NELParser#httpMethods.
NELListenerImpl.prototype.exitHttpMethods = function (ctx) {
};


// Enter a parse tree produced by NELParser#qualifiedName.
NELListenerImpl.prototype.enterQualifiedName = function (ctx) {
};

// Exit a parse tree produced by NELParser#qualifiedName.
NELListenerImpl.prototype.exitQualifiedName = function (ctx) {
};


// Enter a parse tree produced by NELParser#resourceDeclaration.
NELListenerImpl.prototype.enterResourceDeclaration = function (ctx) {
    currentResource.appendParameter({key: "inputParameter", value: ctx.Identifier().getText()});
    currentResource.appendConfigStart(
        "resource " + ctx.resourceName().getText() + "(message " + ctx.Identifier().getText() + ") {\n");
};

// Exit a parse tree produced by NELParser#resourceDeclaration.
NELListenerImpl.prototype.exitResourceDeclaration = function (ctx) {
};


// Enter a parse tree produced by NELParser#resourceName.
NELListenerImpl.prototype.enterResourceName = function (ctx) {
    currentResource.appendParameter({key: "title", value: ctx.Identifier().getText()});
};

// Exit a parse tree produced by NELParser#resourceName.
NELListenerImpl.prototype.exitResourceName = function (ctx) {
};


// Enter a parse tree produced by NELParser#block.
NELListenerImpl.prototype.enterBlock = function (ctx) {
};

// Exit a parse tree produced by NELParser#block.
NELListenerImpl.prototype.exitBlock = function (ctx) {
};


// Enter a parse tree produced by NELParser#blockStatement.
NELListenerImpl.prototype.enterBlockStatement = function (ctx) {
};

// Exit a parse tree produced by NELParser#blockStatement.
NELListenerImpl.prototype.exitBlockStatement = function (ctx) {
};


// Enter a parse tree produced by NELParser#tryCatchBlock.
NELListenerImpl.prototype.enterTryCatchBlock = function (ctx) {
    var tryCatchMediator = new TreeNode("TryCatchMediator", "TryCatchMediator", "", "");
    parentStack.push(tryCatchMediator);
};

// Exit a parse tree produced by NELParser#tryCatchBlock.
NELListenerImpl.prototype.exitTryCatchBlock = function (ctx) {
    var tryCatchBlock = parentStack.pop();
    var currentParent = parentStack.pop();
    if (currentParent) {
        currentParent.getChildren().push(tryCatchBlock);
        parentStack.push(currentParent);
    } else {
        currentResource.getChildren().push(tryCatchBlock);
    }
};


// Enter a parse tree produced by NELParser#tryClause.
NELListenerImpl.prototype.enterTryClause = function (ctx) {
    var tryBlock = new TreeNode("TryBlock", "TryBlock", "try {\n", "}");
    parentStack.push(tryBlock);
};

// Exit a parse tree produced by NELParser#tryClause.
NELListenerImpl.prototype.exitTryClause = function (ctx) {
    var tryBlock = parentStack.pop();
    var tryCatchBlock = parentStack.pop();
    tryCatchBlock.getChildren().push(tryBlock);
    parentStack.push(tryCatchBlock);
};


// Enter a parse tree produced by NELParser#catchClause.
NELListenerImpl.prototype.enterCatchClause = function (ctx) {
    var catchBlock = new TreeNode("CatchBlock", "CatchBlock", "", "}\n");
    catchBlock.appendConfigStart(" catch (");
    catchBlock.setConfigEnd("}\n");
    parentStack.push(catchBlock);
};

// Exit a parse tree produced by NELParser#catchClause.
NELListenerImpl.prototype.exitCatchClause = function (ctx) {
    var catchBlock = parentStack.pop();
    var tryCatchBlock = parentStack.pop();
    tryCatchBlock.getChildren().push(catchBlock);
    parentStack.push(tryCatchBlock);
};


// Enter a parse tree produced by NELParser#exceptionHandler.
NELListenerImpl.prototype.enterExceptionHandler = function (ctx) {
};

// Exit a parse tree produced by NELParser#exceptionHandler.
NELListenerImpl.prototype.exitExceptionHandler = function (ctx) {
    var catchBlock = parentStack.pop();
    catchBlock.appendConfigStart(ctx.Identifier().getText() + ") {\n");
    catchBlock.appendParameter({key: "exception", value: ctx.Identifier().getText()});
    parentStack.push(catchBlock);
};


// Enter a parse tree produced by NELParser#exceptionType.
NELListenerImpl.prototype.enterExceptionType = function (ctx) {
    var catchBlock = parentStack.pop();
    catchBlock.appendConfigStart(ctx.getText() + " ");
    parentStack.push(catchBlock);
};

// Exit a parse tree produced by NELParser#exceptionType.
NELListenerImpl.prototype.exitExceptionType = function (ctx) {
};


// Enter a parse tree produced by NELParser#ifElseBlock.
NELListenerImpl.prototype.enterIfElseBlock = function (ctx) {
    var ifElseBlock = new TreeNode("IfElseMediator", "IfElseMediator", "", "");
    parentStack.push(ifElseBlock);
};

// Exit a parse tree produced by NELParser#ifElseBlock.
NELListenerImpl.prototype.exitIfElseBlock = function (ctx) {
    var ifElseBlock = parentStack.pop();
    var currentParent = parentStack.pop();
    if (currentParent) {
        currentParent.getChildren().push(ifElseBlock);
        parentStack.push(currentParent);
    } else {
        currentResource.getChildren().push(ifElseBlock);
    }
};


// Enter a parse tree produced by NELParser#ifBlock.
NELListenerImpl.prototype.enterIfBlock = function (ctx) {
    var ifBlock = new TreeNode("IfBlock", "IfBlock", "", "}");//without \n
    parentStack.push(ifBlock);
};

// Exit a parse tree produced by NELParser#ifBlock.
NELListenerImpl.prototype.exitIfBlock = function (ctx) {
    var ifBlock = parentStack.pop();
    var condition = ifBlock.parameters[0].value;
    ifBlock.setConfigStart("if (" + condition + ") {\n");
    var ifElseBlock = parentStack.pop();
    ifElseBlock.getChildren().push(ifBlock);
    parentStack.push(ifElseBlock);
};


// Enter a parse tree produced by NELParser#elseBlock.
NELListenerImpl.prototype.enterElseBlock = function (ctx) {
    var elseBlock = new TreeNode("ElseBlock", "ElseBlock", "else {\n", "}\n");
    parentStack.push(elseBlock);
};

// Exit a parse tree produced by NELParser#elseBlock.
NELListenerImpl.prototype.exitElseBlock = function (ctx) {
    var elseBlock = parentStack.pop();
    var ifElseBlock = parentStack.pop();
    ifElseBlock.getChildren().push(elseBlock);
    parentStack.push(ifElseBlock);
};


// Enter a parse tree produced by NELParser#localVariableDeclarationStatement.
NELListenerImpl.prototype.enterLocalVariableDeclarationStatement = function (ctx) {
    var type;
    var variableName;
    var property = {
        key: "",
        name: ""
    };
    if (ctx.type()) {
        type = ctx.type().getText();
    } else if (ctx.classType()) {
        type = ctx.classType().getText();
    }
    variableName = ctx.Identifier().getText();
    property.key = variableName;
    property.name = type;
    currentResource.appendParameter({key: "property", value: property});
    currentResource.appendConfigStart(type + " " + variableName + ";\n");
};

// Exit a parse tree produced by NELParser#localVariableDeclarationStatement.
NELListenerImpl.prototype.exitLocalVariableDeclarationStatement = function (ctx) {
};


// Enter a parse tree produced by NELParser#localVariableInitializationStatement.
NELListenerImpl.prototype.enterLocalVariableInitializationStatement = function (ctx) {

};

// Exit a parse tree produced by NELParser#localVariableInitializationStatement.
NELListenerImpl.prototype.exitLocalVariableInitializationStatement = function (ctx) {

};


// Enter a parse tree produced by NELParser#localVariableAssignmentStatement.
NELListenerImpl.prototype.enterLocalVariableAssignmentStatement = function (ctx) {
    if (ctx.mediatorCall()) {
        //for cases like response = invoke(...etc), keep "response" parameter
        lastMediatorCallVariable = ctx.Identifier().getText();
    }
};

// Exit a parse tree produced by NELParser#localVariableAssignmentStatement.
NELListenerImpl.prototype.exitLocalVariableAssignmentStatement = function (ctx) {
};


// Enter a parse tree produced by NELParser#mediatorCallStatement.
NELListenerImpl.prototype.enterMediatorCallStatement = function (ctx) {
};

// Exit a parse tree produced by NELParser#mediatorCallStatement.
NELListenerImpl.prototype.exitMediatorCallStatement = function (ctx) {
};


// Enter a parse tree produced by NELParser#newTypeObjectCreation.
NELListenerImpl.prototype.enterNewTypeObjectCreation = function (ctx) {
};

// Exit a parse tree produced by NELParser#newTypeObjectCreation.
NELListenerImpl.prototype.exitNewTypeObjectCreation = function (ctx) {
};


// Enter a parse tree produced by NELParser#mediatorCall.
NELListenerImpl.prototype.enterMediatorCall = function (ctx) {
    var mediatorCall = ctx.getText();
    var parameter = {
        key: "",
        value: ""
    };
    //invoke and header mediator
    var mediatorId = ctx.Identifier().getText();
    // call mediator is specified in the language as "invoke". This is a special case.
    if ("invoke" === mediatorId) {


        currentMediator = new TreeNode("InvokeMediator", "InvokeMediator", lastMediatorCallVariable
                                                                           + " = invoke(", ");\n");
        lastMediatorCallVariable = "";

    } else if ("setHeader" === mediatorId) {
        //header mediator
        currentMediator = new TreeNode("HeaderProcessor", "HeaderProcessor", "setHeader(", ");\n");

    } else if ("log" === mediatorId) {

        currentMediator = new TreeNode("LogMediator", "LogMediator", "log(", ");\n");

    }
    var currentParent = parentStack.pop();
    if (currentParent) {
        currentParent.getChildren().push(currentMediator);
        parentStack.push(currentParent);
    } else {
        currentResource.getChildren().push(currentMediator);
    }
};

// Exit a parse tree produced by NELParser#mediatorCall.
NELListenerImpl.prototype.exitMediatorCall = function (ctx) {
    console.log("exitMediatorCall" + count++);
    if (currentMediator.getValue() === "HeaderProcessor") {
        // "setHeader(messageRef = response, headerName = \"HTTP.StatusCode\", headerValue = 500);"
        currentMediator.appendConfigStart(
            "messageRef = " + currentMediator.getParameterValue("messageRef") + ", headerName = "
            + currentMediator.getParameterValue("headerName") + ", headerValue = " + currentMediator.getParameterValue(
                "headerValue"));
    } else if (currentMediator.getValue() === "InvokeMediator") {
//invoke(messageRef = m, endpointRef = stockEP);"+
        currentMediator.appendConfigStart(
            "messageRef = " + currentMediator.getParameterValue("messageRef") + ", endpointRef = "
            + currentMediator.getParameterValue("endpointRef"))
    } else if (currentMediator.getValue() === "LogMediator") {
        //"log(level = \"custom\", status = \"Message Received at getStocksResource\");"
        currentMediator.appendConfigStart(
            "level = " + currentMediator.getParameterValue("level") + ", status = " + currentMediator.getParameterValue(
                "status"))
    }
};


// Enter a parse tree produced by NELParser#endpointDeclaration.
NELListenerImpl.prototype.enterEndpointDeclaration = function (ctx) {
};

// Exit a parse tree produced by NELParser#endpointDeclaration.
NELListenerImpl.prototype.exitEndpointDeclaration = function (ctx) {
};


// Enter a parse tree produced by NELParser#parametersAnnotation.
NELListenerImpl.prototype.enterParametersAnnotation = function (ctx) {
};

// Exit a parse tree produced by NELParser#parametersAnnotation.
NELListenerImpl.prototype.exitParametersAnnotation = function (ctx) {
};


// Enter a parse tree produced by NELParser#circuitBreakerAnnotation.
NELListenerImpl.prototype.enterCircuitBreakerAnnotation = function (ctx) {
};

// Exit a parse tree produced by NELParser#circuitBreakerAnnotation.
NELListenerImpl.prototype.exitCircuitBreakerAnnotation = function (ctx) {
};


// Enter a parse tree produced by NELParser#keyValuePairs.
NELListenerImpl.prototype.enterKeyValuePairs = function (ctx) {
};

// Exit a parse tree produced by NELParser#keyValuePairs.
NELListenerImpl.prototype.exitKeyValuePairs = function (ctx) {
};


// Enter a parse tree produced by NELParser#keyValuePair.
NELListenerImpl.prototype.enterKeyValuePair = function (ctx) {
    //(Identifier | classType) '='  ( literal | Identifier )
    var value, key;

    if (ctx.literal()) {
        //check for double quotes
        value = ctx.literal().getText().replace(/['"]+/g, '');
    } else {
        value =
            ctx.Identifier(ctx.Identifier().length - 1).getText().replace(/['"]+/g, '');
    }
    // if the key is a classType (eg: 'endpoint' or 'message')
    if (ctx.classType()) {
        key = ctx.classType().getText();
    } else {
        key = ctx.Identifier(0).getText();
    }
    var parameter = {
        key: key,
        value: value
    };
    currentMediator.appendParameter(parameter);

};

// Exit a parse tree produced by NELParser#keyValuePair.
NELListenerImpl.prototype.exitKeyValuePair = function (ctx) {
};


// Enter a parse tree produced by NELParser#messageModificationStatement.
NELListenerImpl.prototype.enterMessageModificationStatement = function (ctx) {
};

// Exit a parse tree produced by NELParser#messageModificationStatement.
NELListenerImpl.prototype.exitMessageModificationStatement = function (ctx) {
};


// Enter a parse tree produced by NELParser#returnStatement.
NELListenerImpl.prototype.enterReturnStatement = function (ctx) {
    //Mediator respondMediator = MediatorProviderRegistry.getInstance().getMediator(Constants.RESPOND_MEDIATOR_NAME);
    var respondMediator = {messageId: ""};
    //ParameterHolder parameterHolder = new ParameterHolder();
    if (ctx.Identifier() != null) {
        var messageId = ctx.Identifier().getText();
        respondMediator.messageId = messageId;
    }
    var replyNode = new TreeNode("ResponseMsg", "ResponseMsg", "reply " + messageId, ";\n");

    var currentParent = parentStack.pop();
    if (currentParent) {
        currentParent.getChildren().push(replyNode);
        parentStack.push(currentParent);
    } else {
        currentResource.getChildren().push(replyNode);
    }

};

// Exit a parse tree produced by NELParser#returnStatement.
NELListenerImpl.prototype.exitReturnStatement = function (ctx) {
};


// Enter a parse tree produced by NELParser#parExpression.
NELListenerImpl.prototype.enterParExpression = function (ctx) {
    var conditionWithBrackets = ctx.getText();
    var condition = conditionWithBrackets.substring(1, conditionWithBrackets.length - 1);
    var currentIf = parentStack.pop();
    currentIf.appendParameter({key: "condition", value: condition});
    parentStack.push(currentIf);
};

// Exit a parse tree produced by NELParser#parExpression.
NELListenerImpl.prototype.exitParExpression = function (ctx) {
};


// Enter a parse tree produced by NELParser#expression.
NELListenerImpl.prototype.enterExpression = function (ctx) {
};

// Exit a parse tree produced by NELParser#expression.
NELListenerImpl.prototype.exitExpression = function (ctx) {
};


// Enter a parse tree produced by NELParser#evalExpression.
NELListenerImpl.prototype.enterEvalExpression = function (ctx) {
};

// Exit a parse tree produced by NELParser#evalExpression.
NELListenerImpl.prototype.exitEvalExpression = function (ctx) {
};


// Enter a parse tree produced by NELParser#literal.
NELListenerImpl.prototype.enterLiteral = function (ctx) {
};

// Exit a parse tree produced by NELParser#literal.
NELListenerImpl.prototype.exitLiteral = function (ctx) {
};


// Enter a parse tree produced by NELParser#mediaType.
NELListenerImpl.prototype.enterMediaType = function (ctx) {
    service.producer = {mediaType: ctx.getText()};
};

// Exit a parse tree produced by NELParser#mediaType.
NELListenerImpl.prototype.exitMediaType = function (ctx) {
};


// Enter a parse tree produced by NELParser#type.
NELListenerImpl.prototype.enterType = function (ctx) {
};

// Exit a parse tree produced by NELParser#type.
NELListenerImpl.prototype.exitType = function (ctx) {
};


// Enter a parse tree produced by NELParser#classType.
NELListenerImpl.prototype.enterClassType = function (ctx) {
};

// Exit a parse tree produced by NELParser#classType.
NELListenerImpl.prototype.exitClassType = function (ctx) {
};


// Enter a parse tree produced by NELParser#messagePropertyName.
NELListenerImpl.prototype.enterMessagePropertyName = function (ctx) {
};

// Exit a parse tree produced by NELParser#messagePropertyName.
NELListenerImpl.prototype.exitMessagePropertyName = function (ctx) {
};

exports.NELListenerImpl = NELListenerImpl;