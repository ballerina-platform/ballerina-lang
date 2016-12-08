package org.wso2.ballerina.core.parser.visitor;

import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Import;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;

/**
 * Main visitor class
 */
public class BallerinaBaseVisitorImpl extends BallerinaBaseVisitor {
    BallerinaFile balFile = new BallerinaFile();

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitCompilationUnit(BallerinaParser.CompilationUnitContext ctx) {
        CompilationUnitVisitor compilationUnitVisitor = new CompilationUnitVisitor();

        // Read the package declaration
        Package packageName = (Package) ctx.packageDeclaration().accept(compilationUnitVisitor);
        balFile.setPackageName(packageName.getFullQualifiedName());

        for (BallerinaParser.ImportDeclarationContext idc : ctx.importDeclaration()) {
            Import importObject = (Import) idc.accept(compilationUnitVisitor);
            balFile.addImport(importObject);
        }

        // Read the services
        ServiceVisitor serviceVisitor = new ServiceVisitor();
        for (BallerinaParser.ServiceDefinitionContext sdc : ctx.serviceDefinition()) {
            Service serviceObject = (Service) sdc.accept(serviceVisitor);
            balFile.addService(serviceObject);
        }

        // Read the functions
        FunctionVisitor functionVisitor = new FunctionVisitor();
        for (BallerinaParser.FunctionDefinitionContext fdc : ctx.functionDefinition()) {
            Function functionObject = (Function) fdc.accept(functionVisitor);
            balFile.addFunction(functionObject);
        }

        return compilationUnitVisitor;
    }


}
