define(function(require, exports, module) {
    var Tree = require('./Tree');
    module.exports.Trees = require('./Tree').Trees;
    module.exports.RuleNode = Tree.RuleNode;
    module.exports.ParseTreeListener = Tree.ParseTreeListener;
    module.exports.ParseTreeVisitor = Tree.ParseTreeVisitor;
    module.exports.ParseTreeWalker = Tree.ParseTreeWalker;
});