function enableUndoRedo() {
    Mousetrap.bind(['command+z', 'ctrl+z'], function() {
        webViewRPCHandler.invokeRemoteMethod('undo');
        return true;
    });
    Mousetrap.bind(['command+shift+z', 'ctrl+shift+z'], function() {
        webViewRPCHandler.invokeRemoteMethod('redo');
        return true;
    });
}