import * as codepoints from "./codepoints.json";

const cps: any = codepoints;

export function getCodePoint(name: string): string {
    return cps[name];
}

export function getAllCodePoints(): any {
    return cps;
}
