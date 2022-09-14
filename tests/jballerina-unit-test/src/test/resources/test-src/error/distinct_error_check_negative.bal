type Error distinct error;

type ClientError distinct Error;
type ListenerError distinct Error;

type GenericClientError distinct ClientError;

type PayloadBindingClientError distinct ClientError;
type PayloadBindingListenerError distinct ListenerError;
type PayloadBindingError PayloadBindingClientError | PayloadBindingListenerError;
type PayloadBindingError1 distinct ClientError | distinct ListenerError;
type PayloadBindingError2 ClientError | distinct ListenerError;

function getGenericClientError() returns ClientError {
    return error GenericClientError("Whoops!");
}

function getPayloadBindingError1() returns ClientError {
    return error PayloadBindingClientError("Whoops!");
}

function getPayloadBindingError2() returns ClientError {
    PayloadBindingError1 err = error ClientError("Whoops!");
    return <ClientError> err;
}

function testDistinctError() {
    var r = getGenericClientError();
    _ = r is PayloadBindingError; // getting hint: always true,
                                  // but result is false because runtime is not changed yet.

    r = getPayloadBindingError1();
    _ = r is PayloadBindingError; // hint: always true

    r = getGenericClientError();
    _ = r is PayloadBindingError1; // hint: always true

    r = getPayloadBindingError2();
    _ = r is PayloadBindingError1; // hint: always true

    _ = r is PayloadBindingError2; // hint: always true
}
