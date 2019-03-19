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


http:Client clientEP3 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:CLIENT_CREDENTIALS_GRANT,
                config: {
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/body",
                    clientId: "3MVG9YDQS5WtC11paU2WcQjBB3L5w4gz52uriT8ksZ3nUVjKvrfQMrU4uvZohTftxStwNEW4cfStBEGRxRL68",
                    clientSecret: "9205371918321623741",
                    scopes: ["token-scope1", "token-scope2"],
                    credentialBearer: http:POST_BODY_BEARER
                }
            }
        }
    });

http:Client clientEP4 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:CLIENT_CREDENTIALS_GRANT,
                config: {
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/body",
                    clientId: "invalid_client_id",
                    clientSecret: "invalid_client_secret",
                    scopes: ["token-scope1", "token-scope2"],
                    credentialBearer: http:POST_BODY_BEARER
                }
            }
        }
    });

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

http:Client clientEP8 = new("https://localhost:9095", config = {
        auth: {
            scheme: http:OAUTH2,
            config: {
                grantType: http:PASSWORD_GRANT,
                config: {
                    tokenUrl: "https://localhost:9196/oauth2/token/authorize/none",
                    username: "johndoe",
                    password: "A3ddj3w",
                    scopes: ["token-scope1", "token-scope2"],
                    credentialBearer: http:NO_BEARER
                }
            }
        }
    });

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

http:Client clientEP10 = new("https://localhost:9095", config = {
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

http:Client clientEP11 = new("https://localhost:9095", config = {
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

http:Client clientEP12 = new("https://localhost:9095", config = {
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
    if (args[0] == "CLIENT_CREDENTIALS_GRANT_TYPE") {
        var resp = clientEP1->post("/foo/bar", req);
    } else if (args[0] == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_INVALID_CREDENTIALS") {
        var resp = clientEP2->post("/foo/bar", req);
    } else if (args[0] == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_POST_BODY_BEARER") {
        var resp = clientEP3->post("/foo/bar", req);
    } else if (args[0] == "CLIENT_CREDENTIALS_GRANT_TYPE_WITH_POST_BODY_BEARER_AND_INVALID_CREDENTIALS") {
        var resp = clientEP4->post("/foo/bar", req);
    } else if (args[0] == "PASSWORD_GRANT_TYPE") {
        var resp = clientEP5->post("/foo/bar", req);
    } else if (args[0] == "PASSWORD_GRANT_TYPE_WITH_REFRESH_CONFIG") {
        var resp = clientEP6->post("/foo/bar", req);
    } else if (args[0] == "PASSWORD_GRANT_TYPE_WITH_INVALID_USERNAME_PASSWORD") {
        var resp = clientEP7->post("/foo/bar", req);
    } else if (args[0] == "PASSWORD_GRANT_TYPE_WITH_NO_BEARER") {
        var resp = clientEP8->post("/foo/bar", req);
    }else if (args[0] == "DIRECT_TOKEN") {
        var resp = clientEP9->post("/foo/bar", req);
    } else if (args[0] == "DIRECT_TOKEN_WITH_INVALID_ACCESS_TOKEN") {
        var resp = clientEP10->post("/foo/bar", req);
    } else if (args[0] == "DIRECT_TOKEN_WITH_INVALID_ACCESS_TOKEN_AND_RETRY_REQUEST_FALSE") {
        var resp = clientEP11->post("/foo/bar", req);
    } else if (args[0] == "DIRECT_TOKEN_WITH_INVALID_ACCESS_TOKEN_AND_INVALID_REFRESH_TOKEN") {
        var resp = clientEP12->post("/foo/bar", req);
    }
}
