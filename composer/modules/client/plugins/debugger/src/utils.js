/**
 * @description Get package name from astRoot
 * @param {ASTModel} astRoot
 * @returns
 * @memberof TreeUtil
 */
export function getPackageNameString(astRoot) {
    if (!astRoot) {
        return '.';
    }
    const packageDeclaration = astRoot.filterTopLevelNodes({ kind: 'PackageDeclaration' });
    if (!packageDeclaration || !packageDeclaration[0]) {
        return '.';
    }
    return packageDeclaration[0].getPackageNameString();
}