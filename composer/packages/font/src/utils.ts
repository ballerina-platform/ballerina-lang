import * as codepoints from "./codepoints.json";

export function getCodePoint(name: string): string {
    const cps: any = codepoints;
    return cps[name];
}
