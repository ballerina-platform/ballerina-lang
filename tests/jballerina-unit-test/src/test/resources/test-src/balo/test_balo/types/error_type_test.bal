import testorg/errors version v1 as er;

int i = 0;

function getApplicationError() returns error {
    er:ApplicationError e = error(message = "Client has been stopped");
    return e;
}
