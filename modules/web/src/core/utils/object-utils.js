/**
 * Make given object imutable
 *
 * @param {Object} object to freeze
 */
export function makeImutable(o) {
    Object.freeze(o);
    Object.preventExtensions(0);
    if (o === undefined) {
        return o;
    }
    Object.getOwnPropertyNames(o).forEach((prop) => {
        if (o[prop] !== null
        && (typeof o[prop] === 'object' || typeof o[prop] === 'function')
        && !Object.isFrozen(o[prop])) {
            makeImutable(o[prop]);
        }
    });

    return o;
}
