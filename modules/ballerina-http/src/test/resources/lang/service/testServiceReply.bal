import ballerina.net.http;

service<http> echo {

    resource echo (message m) {
        reply m;
    }

    resource testEmptyResourceBody (message m) {
    }

    resource testReplyArrayReturn (message m) {
        message[] data = [];
        data[0] = m;
        reply data[0];
    }

    resource testFunctionReturn (message m) {
        reply getEmptyMessage();
    }
}

function getEmptyMessage () returns (message m) {
    m = {};
    return;
}
