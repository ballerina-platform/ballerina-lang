import axios from "axios";

export const BALLERINA_LANGUAGE_ID = 'ballerina';
export const BALLERINA_LANGUAGE_NAME = 'Ballerina';
export interface ParserReply {
    model?: Object
}

export function parseContent(content: String) : Promise<ParserReply> {
    const parseOpts = {
        content,
        includePackageInfo: true,
        includeProgramDir: true,
        includeTree: true,
    }
    return axios.post(
                    'https://parser.playground.preprod.ballerina.io/api/parser',
                    parseOpts,
                    { 
                        headers: {
                            'content-type': 'application/json; charset=utf-8',
                        } 
                    })
                .then(response => response.data);
}