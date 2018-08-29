exports.getAST = (langClient, uri) => {
    const args = {
        documentIdentifier: {
            uri,
        }
    }
    return langClient.sendRequest("ballerinaDocument/ast", args);
}