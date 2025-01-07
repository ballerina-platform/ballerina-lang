import ballerina/test;

type ErrorDetail record {
    map<string|string[]> headers?;
    anydata body?;
};

type DefaultErrorDetail record {
    *ErrorDetail;
    int statusCode?;
};

type StatusCodeError distinct error<ErrorDetail>;

type '4XXStatusCodeError distinct StatusCodeError;

type '5XXStatusCodeError distinct StatusCodeError;

type DefaultStatusCodeError distinct StatusCodeError & error<DefaultErrorDetail>;

type BadRequestError distinct '4XXStatusCodeError;

type UnauthorizedError distinct '4XXStatusCodeError;

type PaymentRequiredError distinct '4XXStatusCodeError;

type ForbiddenError distinct '4XXStatusCodeError;

type NotFoundError distinct '4XXStatusCodeError;

type MethodNotAllowedError distinct '4XXStatusCodeError;

type NotAcceptableError distinct '4XXStatusCodeError;

type ProxyAuthenticationRequiredError distinct '4XXStatusCodeError;

type RequestTimeoutError distinct '4XXStatusCodeError;

type ConflictError distinct '4XXStatusCodeError;

type GoneError distinct '4XXStatusCodeError;

type LengthRequiredError distinct '4XXStatusCodeError;

type PreconditionFailedError distinct '4XXStatusCodeError;

type PayloadTooLargeError distinct '4XXStatusCodeError;

type URITooLongError distinct '4XXStatusCodeError;

type UnsupportedMediaTypeError distinct '4XXStatusCodeError;

type RangeNotSatisfiableError distinct '4XXStatusCodeError;

type ExpectationFailedError distinct '4XXStatusCodeError;

type MisdirectedRequestError distinct '4XXStatusCodeError;

type UnprocessableEntityError distinct '4XXStatusCodeError;

type LockedError distinct '4XXStatusCodeError;

type FailedDependencyError distinct '4XXStatusCodeError;

type UpgradeRequiredError distinct '4XXStatusCodeError;

type PreconditionRequiredError distinct '4XXStatusCodeError;

type TooManyRequestsError distinct '4XXStatusCodeError;

type RequestHeaderFieldsTooLargeError distinct '4XXStatusCodeError;

type UnavailableDueToLegalReasonsError distinct '4XXStatusCodeError;

type InternalServerErrorError distinct '5XXStatusCodeError;

type NotImplementedError distinct '5XXStatusCodeError;

type BadGatewayError distinct '5XXStatusCodeError;

type ServiceUnavailableError distinct '5XXStatusCodeError;

type GatewayTimeoutError distinct '5XXStatusCodeError;

type HTTPVersionNotSupportedError distinct '5XXStatusCodeError;

type VariantAlsoNegotiatesError distinct '5XXStatusCodeError;

type InsufficientStorageError distinct '5XXStatusCodeError;

type LoopDetectedError distinct '5XXStatusCodeError;

type NotExtendedError distinct '5XXStatusCodeError;

type NetworkAuthenticationRequiredError distinct '5XXStatusCodeError;

type UnionType
        BadRequestError|UnauthorizedError|PaymentRequiredError|
        ForbiddenError|NotFoundError|MethodNotAllowedError|
        NotAcceptableError|ProxyAuthenticationRequiredError|RequestTimeoutError|
        ConflictError|GoneError|LengthRequiredError|PreconditionFailedError|
        PayloadTooLargeError|URITooLongError|UnsupportedMediaTypeError|
        RangeNotSatisfiableError|ExpectationFailedError|MisdirectedRequestError|
        UnprocessableEntityError|LockedError|FailedDependencyError|
        UpgradeRequiredError|PreconditionRequiredError|TooManyRequestsError|
        RequestHeaderFieldsTooLargeError|UnavailableDueToLegalReasonsError|
        InternalServerErrorError|NotImplementedError|BadGatewayError|
        ServiceUnavailableError|GatewayTimeoutError|HTTPVersionNotSupportedError|
        VariantAlsoNegotiatesError|InsufficientStorageError|LoopDetectedError|
        NotExtendedError|NetworkAuthenticationRequiredError|DefaultStatusCodeError;

function test() {
    UnionType err = createMember(1);
    test:assertTrue(err is BadRequestError);
}

function createMember(int id) returns UnionType {
    if (id == 1) {
        BadRequestError err = error("Bad Request");
        return err;
    }
    UnauthorizedError err = error("Unauthorized");
    return err;
}
