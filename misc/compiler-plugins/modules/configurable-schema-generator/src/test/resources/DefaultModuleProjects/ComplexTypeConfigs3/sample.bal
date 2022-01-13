enum CredentialBearer {
    AUTH_HEADER_BEARER,
    POST_BODY_BEARER
}

enum HttpVersion {
    HTTP_1_1,
    HTTP_2
}

type ClientCredentialsGrantConfig record {|
    string tokenUrl;
    string clientId;
    string clientSecret;
    string[] scopes?;
    decimal defaultTokenExpTime = 3600;
    decimal clockSkew = 0;
    map<string> optionalParams?;
    CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
    ClientConfiguration clientConfig = {};
|};

type PasswordGrantConfig record {|
    string tokenUrl;
    string username;
    string password;
    string clientId?;
    string clientSecret?;
    string[] scopes?;
    record {|
        string refreshUrl;
        string[] scopes?;
        map<string> optionalParams?;
        CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
        ClientConfiguration clientConfig = {};
    |} refreshConfig?;
    decimal defaultTokenExpTime = 3600;
    decimal clockSkew = 0;
    map<string> optionalParams?;
    CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
    ClientConfiguration clientConfig = {};
|};

type RefreshTokenGrantConfig record {|
    string refreshUrl;
    string refreshToken;
    string clientId;
    string clientSecret;
    string[] scopes?;
    decimal defaultTokenExpTime = 3600;
    decimal clockSkew = 0;
    map<string> optionalParams?;
    CredentialBearer credentialBearer = AUTH_HEADER_BEARER;
    ClientConfiguration clientConfig = {};
|};

type ClientAuth ClientCredentialsGrantConfig|PasswordGrantConfig|RefreshTokenGrantConfig;

type ClientConfiguration record {|
    HttpVersion httpVersion = HTTP_1_1;
    map<string> customHeaders?;
    string customPayload?;
    ClientAuth auth?;
    SecureSocket secureSocket?;
|};

type TrustStore record {|
    string path;
    string password;
|};

type KeyStore record {|
    string path;
    string password;
|};

type CertKey record {|
   string certFile;
   string keyFile;
   string keyPassword?;
|};

type SecureSocket record {|
    boolean disable = false;
    TrustStore|string cert?;
    KeyStore|CertKey key?;
|};

public type OAuth2RefreshTokenGrantConfig record {|
    *RefreshTokenGrantConfig;
|};

// Google sheets configuration parameters
@display {
    kind: "OAuthConfig",
    provider: "Google Sheets",
    label: "Set Up Google Sheets Connection",
    description: "Set Up Google Sheets Connection"
}
configurable OAuth2RefreshTokenGrantConfig & readonly sheetOAuthConfig = ?;

@display {
    kind: "ConnectionField",
    connectionRef: "sheetOauthConfig",
    provider: "Google Sheets",
    operationName: "getAllSpreadsheets",
    label: "Spreadsheet Name",
    description: "Spreadsheet Name"
}
configurable string & readonly spreadsheetId = ?;

@display {
    kind: "ConnectionField",
    connectionRef: "sheetOauthConfig",
    argRef: "spreadsheetId",
    provider: "Google Sheets",
    operationName: "getSheetList",
    label: "Worksheet Name",
    description: "Worksheet Name"
}
configurable string & readonly worksheetName = ?;

// Google gmail configuration parameters
@display {
    kind: "OAuthConfig",
    provider: "Gmail",
    label: "Set Up Gmail Connection",
    description: "Set Up Gmail Connection"
}
configurable OAuth2RefreshTokenGrantConfig & readonly gmailOauthConfig = ?;

@display {label: "Recipient's Email", description: "Recipient's Email"}
configurable string & readonly recipientAddress = ?;

@display {label: "Sender's Email", description: "Sender's Email"}
configurable string & readonly senderAddress = ?;
