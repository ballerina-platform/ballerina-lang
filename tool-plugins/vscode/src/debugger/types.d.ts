declare module 'ps-node' {
    namespace ps {
        export function lookup(args: any, callback: any) : void;
        export function kill(args: any) : void;
    }
    export default ps;
}

declare module 'openport' {
    namespace openport {
        export function find(args: any) : void;
    }
    export default openport;
}