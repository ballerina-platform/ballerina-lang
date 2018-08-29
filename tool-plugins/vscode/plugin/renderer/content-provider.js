class StaticProvider {
    provideTextDocumentContent(uri) {
        return require(`.${uri.path}`);
    }
}

module.exports = {
    StaticProvider,
};