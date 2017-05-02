/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

export const panel = {
    wrapper: {
        gutter: {
            v: 50,
            h: 50
        }
    },
    heading: {
        padding: {
            top: 0,
            right: 0,
            bottom: 0,
            left: 0
        },
        height:25
    },
    body: {
        padding: {
            top: 50,
            right: 50,
            bottom: 50,
            left: 50
        },
        height: 200
    }
};

export const innerPanel = {
    wrapper: {
        gutter: {
            v: 50,
            h: 50
        }
    },
    heading: {
        padding: {
            top: 0,
            right: 0,
            bottom: 0,
            left: 0
        }
    },
    body: {
        padding: {
            top: 50,
            right: 50,
            bottom: 50,
            left: 50
        }
    }
};

export const lifeLine = {
    width: 120,
    head: {
        height: 30
    },
    footer: {
        height: 30
    },
    line: {
        height: 320
    },
    gutter: {
        v: 50,
        h: 50
    }
};

export const statement = {
    width: 120,
    height: 30,
    gutter: {
        v: 25,
        h: 0
    },
    padding: {
        top: 5,
        right: 5,
        bottom: 5,
        left: 5
    },
    maxWidth: 300
};

export const blockStatement = {
    heading : {
        width: 50,
        height: statement.height
    },
    body: {
        padding: {
            top: 50,
            right: 50,
            bottom: 50,
            left: 50
        },
        height: 200
    }
};

export const canvas = {
    body: {
        padding: {
            top: 50,
            right: 50,
            bottom: 50,
            left: 50
        },
    }
};

export const statementContainer = {
    width: 120,
    height: 400
};
