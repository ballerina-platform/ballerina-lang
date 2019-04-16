// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/http;

// Test the client credentials grant type with valid credentials
http:Client clientEP1 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:CLIENT_CREDENTIALS_GRANT,
                config: {
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
                    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                    clientSecret: "9205371918321623741",
                    scopes: ["token-scope1", "token-scope2"]
                }
            }
        }
    });

// Test the client credentials grant type with invalid client credentials
http:Client clientEP2 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:CLIENT_CREDENTIALS_GRANT,
                config: {
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
                    clientId: "invalid_client_id",
                    clientSecret: "invalid_client_secret",
                    scopes: ["token-scope1", "token-scope2"]
                }
            }
        }
    });

// Test the client credentials grant type with a post-body bearer and valid credentials
http:Client clientEP3 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:CLIENT_CREDENTIALS_GRANT,
                config: {
                    credentialBearer: http:POST_BODY_BEARER,
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/body",
                    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                    clientSecret: "9205371918321623741",
                    scopes: ["token-scope1", "token-scope2"]
                }
            }
        }
    });

// Test the client credentials grant type with a post-body bearer and invalid credentials
http:Client clientEP4 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:CLIENT_CREDENTIALS_GRANT,
                config: {
                    credentialBearer: http:POST_BODY_BEARER,
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/body",
                    clientId: "invalid_client_id",
                    clientSecret: "invalid_client_secret",
                    scopes: ["token-scope1", "token-scope2"]
                }
            }
        }
    });

// Test the password grant type with valid credentials
http:Client clientEP5 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:PASSWORD_GRANT,
                config: {
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
                    username: "johndoe",
                    password: "A3ddj3w",
                    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                    clientSecret: "9205371918321623741",
                    scopes: ["token-scope1", "token-scope2"]
                }
            }
        }
    });

// Test the password grant type with valid credentials and a valid refresh config
http:Client clientEP6 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:PASSWORD_GRANT,
                config: {
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
                    username: "johndoe",
                    password: "A3ddj3w",
                    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                    clientSecret: "9205371918321623741",
                    scopes: ["token-scope1", "token-scope2"],
                    refreshConfig: {
                        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
                        scopes: ["token-scope1", "token-scope2"]
                    }
                }
            }
        }
    });

// Test the password grant type with an invalid username, password, and a valid refresh config
http:Client clientEP7 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:PASSWORD_GRANT,
                config: {
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/header",
                    username: "invalid_username",
                    password: "invalid_password",
                    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                    clientSecret: "9205371918321623741",
                    scopes: ["token-scope1", "token-scope2"],
                    refreshConfig: {
                        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
                        scopes: ["token-scope1", "token-scope2"]
                    }
                }
            }
        }
    });

// Test the password grant type with a bearer without credentials and a valid username and password
http:Client clientEP8 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:PASSWORD_GRANT,
                config: {
                    credentialBearer: http:NO_BEARER,
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/none",
                    username: "johndoe",
                    password: "A3ddj3w",
                    scopes: ["token-scope1", "token-scope2"]
                }
            }
        }
    });

// Test the direct token mode with valid credentials and without a refresh config
http:Client clientEP9 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:DIRECT_TOKEN,
                config: {
                    accessToken: "2YotnFZFEjr1zCsicMWpAA"
                }
            }
        }
    });

// Test the direct token mode with an invalid access token and without a refresh config
http:Client clientEP10 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:DIRECT_TOKEN,
                config: {
                    accessToken: "invalid_access_token"
                }
            }
        }
    });

// Test the direct token mode with an invalid access token and a valid refresh config
http:Client clientEP11 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:DIRECT_TOKEN,
                config: {
                    accessToken: "invalid_access_token",
                    refreshConfig: {
                        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
                        refreshToken: "XlfBs91yquexJqDaKEMzVg==",
                        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                        clientSecret: "9205371918321623741",
                        scopes: ["token-scope1", "token-scope2"]
                    }
                }
            }
        }
    });

// Test the direct token mode (with the retrying request set as false) with an invalid access token and without a refresh config
http:Client clientEP12 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:DIRECT_TOKEN,
                config: {
                    accessToken: "invalid_access_token",
                    retryRequest: false
                }
            }
        }
    });

// Test the direct token mode (with the retrying request set as false) with an invalid access token and a valid refresh config
http:Client clientEP13 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:DIRECT_TOKEN,
                config: {
                    accessToken: "invalid_access_token",
                    retryRequest: false,
                    refreshConfig: {
                        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
                        refreshToken: "XlfBs91yquexJqDaKEMzVg==",
                        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                        clientSecret: "9205371918321623741",
                        scopes: ["token-scope1", "token-scope2"]
                    }
                }
            }
        }
    });

// Test the direct token mode with an invalid access token and an invalid refresh config
http:Client clientEP14 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:DIRECT_TOKEN,
                config: {
                    accessToken: "invalid_access_token",
                    refreshConfig: {
                        refreshUrl: "https://localhost:9196/oauth2/token/refresh",
                        refreshToken: "invalid_refresh_token",
                        clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                        clientSecret: "9205371918321623741",
                        scopes: ["token-scope1", "token-scope2"]
                    }
                }
            }
        }
    });

public function main(string... args) {
    http:Request req = new;
    if (args[0] == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_VALID_CREDENTIALS") {
        var resp = clientEP1->post("/foo/bar", req);
    } else if (args[0] == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_INVALID_CREDENTIALS") {
        var resp = clientEP2->post("/foo/bar", req);
    } else if (args[0] == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_POST_BODY_BEARER_AND_VALID_CREDENTIALS") {
        var resp = clientEP3->post("/foo/bar", req);
    } else if (args[0] == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_POST_BODY_BEARER_AND_INVALID_CREDENTIALS") {
        var resp = clientEP4->post("/foo/bar", req);
    } else if (args[0] == "PASSWORD_GRANT_TYPE_WITH_VALID_CREDENTIALS") {
        var resp = clientEP5->post("/foo/bar", req);
    } else if (args[0] == "PASSWORD_GRANT_TYPE_WITH_VALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG") {
        var resp = clientEP6->post("/foo/bar", req);
    } else if (args[0] == "PASSWORD_GRANT_TYPE_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG") {
        var resp = clientEP7->post("/foo/bar", req);
    } else if (args[0] == "PASSWORD_GRANT_TYPE_WITH_NO_BEARER_AND_VALID_CREDENTIALS") {
        var resp = clientEP8->post("/foo/bar", req);
    }else if (args[0] == "DIRECT_TOKEN_WITH_VALID_CREDENTIALS_AND_NO_REFRESH_CONFIG") {
        var resp = clientEP9->post("/foo/bar", req);
    } else if (args[0] == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_NO_REFRESH_CONFIG") {
        var resp = clientEP10->post("/foo/bar", req);
    } else if (args[0] == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG") {
        var resp = clientEP11->post("/foo/bar", req);
    } else if (args[0] == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_NO_REFRESH_CONFIG_BUT_RETRY_REQUEST_FALSE") {
        var resp = clientEP12->post("/foo/bar", req);
    } else if (args[0] == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_VALID_REFRESH_CONFIG_BUT_RETRY_REQUEST_FALSE") {
        var resp = clientEP13->post("/foo/bar", req);
    } else if (args[0] == "DIRECT_TOKEN_WITH_INVALID_CREDENTIALS_AND_INVALID_REFRESH_CONFIG") {
        var resp = clientEP14->post("/foo/bar", req);
    }
}
