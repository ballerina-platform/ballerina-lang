exports.parseContent = (langClient, content) => {
    const parseOpts = {
        content,
        filename: 'file.bal',
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }
    return langClient.sendRequest("ballerinaParser/parseContent", parseOpts);
}