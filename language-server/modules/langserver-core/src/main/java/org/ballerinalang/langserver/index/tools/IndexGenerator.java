package org.ballerinalang.langserver.index.tools;

import org.ballerinalang.langserver.common.utils.completion.BLangFunctionUtil;
import org.ballerinalang.langserver.common.utils.completion.BLangPackageUtil;
import org.ballerinalang.langserver.common.utils.index.DTOUtil;
import org.ballerinalang.langserver.index.dto.BLangFunctionDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BLangResourceDTO;
import org.ballerinalang.langserver.index.dto.BLangServiceDTO;
import org.ballerinalang.langserver.compiler.LSContextManager;
import org.ballerinalang.langserver.compiler.LSPackageLoader;
import org.ballerinalang.langserver.index.LSIndexImpl;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default index is generator.
 */
public class IndexGenerator {

    private static final Logger logger = LoggerFactory.getLogger(IndexGenerator.class);

    private List<BPackageSymbol> getBLangPackages() {
        List<BPackageSymbol> bPackageSymbols = new ArrayList<>();
        CompilerContext tempCompilerContext = LSContextManager.getInstance().getBuiltInPackagesCompilerContext();
        PackageID packageID = new PackageID(new org.wso2.ballerinalang.compiler.util.Name("ballerina"),
                new org.wso2.ballerinalang.compiler.util.Name("http"),
                new org.wso2.ballerinalang.compiler.util.Name(""));
        try {
            // We will wrap this with a try catch to prevent LS crashing due to compiler errors.
            bPackageSymbols.add(LSPackageLoader.getPackageSymbolById(tempCompilerContext, packageID));
        } catch (Exception e) {
            // TODO: Handle Properly
        }
        return bPackageSymbols;
    }
        
    public static void main(String[] args) {
        System.setProperty("ballerina.home", "/media/nadeeshaan/f6e45128-5272-4b10-99c9-b5dc9990d202/nadeeshaan/Development/NextGen_ESB/Bal_Workspace/ballerina-tools-0.973.1-SNAPSHOT");
        IndexGenerator indexGenerator = new IndexGenerator();
        LSIndexImpl.getInstance().init(null);
        List<BPackageSymbol> bPackageSymbols = indexGenerator.getBLangPackages();
        List<BPackageSymbolDTO> bPackageSymbolDTOs = bPackageSymbols.stream()
                .map(DTOUtil::getBLangPackageDTO).collect(Collectors.toList());
        indexGenerator.insertBLangPackages(bPackageSymbols);
        String saveDumpPath = Paths.get("modules/langserver-core/target/").toAbsolutePath().toString();
        LSIndexImpl.getInstance()
                .saveIndexDump(Paths.get(new File(saveDumpPath + File.separator + "indexDump.sql").toURI()));
    }

    private void insertBLangPackages(List<BPackageSymbol> bPackageSymbols) {
        List<Integer> generatedPkgKeys;
//        List<List<BLangService>> serviceLists = new ArrayList<>();
//        List<List<BLangFunction>> functionLists = new ArrayList<>();
//        List<BLangPackageUtil.ObjectCategories> objectCategoriesList = new ArrayList<>();
//        List<List<BLangRecord>> recordLists = new ArrayList<>();
//        List<BLangPackageDTO> packageDTOs = bLangPackages.stream().map(bLangPackage -> {
//            serviceLists.add(bLangPackage.getServices());
//            functionLists.add(bLangPackage.getFunctions());
//            objectCategoriesList.add(BLangPackageUtil.getObjectCategories(bLangPackage));
//            recordLists.add(bLangPackage.getRecords());
//            return this.getBLangPackageDTO(bLangPackage.packageID);
//        }).collect(Collectors.toList());
//        try {
//            generatedPkgKeys = LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangPackages(packageDTOs);
//            for (int i = 0; i < generatedPkgKeys.size(); i++) {
//                insertBLangServices(generatedPkgKeys.get(i), serviceLists.get(i));
//                insertBLangFunctions(generatedPkgKeys.get(i), functionLists.get(i));
//                insertBLangRecords(generatedPkgKeys.get(i), recordLists.get(i));
//                insertBLangObjects(generatedPkgKeys.get(i), objectCategoriesList.get(i));
//            }
//            ResultSet rs = LSIndexImpl.getInstance().getQueryProcessor().selectObjectsByType();
//            while (rs.next()) {
//                System.out.println(rs.getString(4));
//            }
//        } catch (SQLException e) {
//            // TODO: Handle Properly
//        }
    }

//    private void insertBLangServices(int pkgEntryId, List<BLangService> bLangServices) {
//        List<Integer> generatedKeys;
//        List<List<BLangResource>> bLangResources = new ArrayList<>();
//        List<BLangServiceDTO> bLangServiceDTOs = bLangServices.stream().map(bLangService -> {
//            bLangResources.add(bLangService.getResources());
//            return this.getServiceDTO(pkgEntryId, bLangService);
//        }).collect(Collectors.toList());
//        try {
//            generatedKeys = LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangServices(bLangServiceDTOs);
//            for (int i = 0; i < generatedKeys.size(); i++) {
//                this.insertBLangResources(generatedKeys.get(i), bLangResources.get(i));
//            }
//        } catch (SQLException e) {
//            // TODO: Handle Properly
//        }
//    }

//    private void insertBLangFunctions(int pkgEntryId, List<BLangFunction> bLangFunctions) {
//        List<BLangFunctionDTO> bLangFunctionDTOs = bLangFunctions.stream()
//                .map(bLangFunction -> this.getFunctionDTO(pkgEntryId, bLangFunction))
//                .collect(Collectors.toList());
//        try {
//            LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangFunctions(bLangFunctionDTOs);
//        } catch (SQLException | IOException e) {
//            // TODO: Handle Properly
//        }
//    }

//    private void insertBLangRecords(int pkgEntryId, List<BLangRecord> bLangRecords) {
//        List<BLangRecordDTO> bLangRecordDTOs = bLangRecords.stream()
//                .map(record -> this.getRecordDTO(pkgEntryId, record))
//                .collect(Collectors.toList());
//        try {
//            LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangRecords(bLangRecordDTOs);
//        } catch (SQLException | IOException e) {
//            // TODO: Handle Properly
//        }
//    }

    private void insertBLangObjects(int pkgEntryId, BLangPackageUtil.ObjectCategories categories) {
//        insertBLangObjects(pkgEntryId, categories.getEndpoints(), ObjectType.ENDPOINT);
//        insertBLangObjects(pkgEntryId, categories.getCallers(), ObjectType.CALLER);
//        insertBLangObjects(pkgEntryId, categories.getObjects(), ObjectType.OBJECT);
    }

//    private void insertBLangObjects(int pkgEntryId, List<BLangObject> bLangObjects, ObjectType type) {
//        List<BLangObjectDTO> bLangObjectDTOs = bLangObjects.stream()
//                .map(object -> this.getObjectDTO(pkgEntryId, object, type))
//                .collect(Collectors.toList());
//        try {
//            LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangObjects(bLangObjectDTOs);
//        } catch (SQLException | IOException e) {
//            e.printStackTrace();
//            // TODO: Handle Properly
//        }
//    }

//    private void insertBLangResources(int serviceEntryId, List<BLangResource> bLangResources) {
//        List<BLangResourceDTO> bLangResourceDTOs = bLangResources.stream()
//                .map(resource -> this.getResourceDTO(serviceEntryId, resource))
//                .collect(Collectors.toList());
//        try {
//            LSIndexImpl.getInstance().getQueryProcessor().batchInsertBLangResources(bLangResourceDTOs);
//        } catch (SQLException e) {
//            // TODO: Handle Properly
//        }
//    }
}
