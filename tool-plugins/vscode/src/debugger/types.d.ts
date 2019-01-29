
// Declare types of deps without type declarations.
declare module 'ps-node' {
    export function lookup(args: any, callback: any) : void;
    export function kill(args: any) : void;
}

declare module 'openport' {
    export function find(args: any) : void;
}

declare module 'ms-wmic' {
    export function execute(args: any, callback?: any) : void;
}