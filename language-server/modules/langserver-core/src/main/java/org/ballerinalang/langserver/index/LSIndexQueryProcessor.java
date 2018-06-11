package org.ballerinalang.langserver.index;

import org.ballerinalang.langserver.common.utils.index.DAOUtil;
import org.ballerinalang.langserver.common.utils.index.DTOUtil;
import org.ballerinalang.langserver.index.dao.ObjectDAO;
import org.ballerinalang.langserver.index.dao.PackageFunctionDAO;
import org.ballerinalang.langserver.index.dao.RecordDAO;
import org.ballerinalang.langserver.index.dto.BFunctionDTO;
import org.ballerinalang.langserver.index.dto.BObjectTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BRecordTypeSymbolDTO;
import org.ballerinalang.langserver.index.dto.BLangResourceDTO;
import org.ballerinalang.langserver.index.dto.BLangServiceDTO;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class LSIndexQueryProcessor {
    private Connection connection;
 
    private PreparedStatement insertBLangPackage;
 
    private PreparedStatement insertBLangService;
 
    private PreparedStatement insertBLangResource;
 
    private PreparedStatement insertBLangFunction;
 
    private PreparedStatement insertBLangRecord;
 
    private PreparedStatement insertBLangObject;
 
    private PreparedStatement updateActionHolderId;
 
    private PreparedStatement getBLangPackageByOrg;

    private PreparedStatement getFunctionsFromPackage;
    
    private PreparedStatement getRecordsFromPackage;
    
    private PreparedStatement getObjectsFromPackage;
 
    private PreparedStatement selectAllFunctions;

    private PreparedStatement selectAllObjectsByType;


    LSIndexQueryProcessor(Connection connection) {
        // Generate the prepared statements
        try {
            insertBLangPackage = connection.prepareStatement(Constants.INSERT_BLANG_PACKAGE, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangService = connection.prepareStatement(Constants.INSERT_BLANG_SERVICE, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangResource = connection.prepareStatement(Constants.INSERT_BLANG_RESOURCE, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangFunction = connection.prepareStatement(Constants.INSERT_BLANG_FUNCTION, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangRecord = connection.prepareStatement(Constants.INSERT_BLANG_RECORD, 
                    Statement.RETURN_GENERATED_KEYS);
            insertBLangObject = connection.prepareStatement(Constants.INSERT_BLANG_OBJECT, 
                    Statement.RETURN_GENERATED_KEYS);
            updateActionHolderId = connection.prepareStatement(Constants.UPDATE_ENDPOINT_ACTION_HOLDER_ID, 
                    Statement.RETURN_GENERATED_KEYS);
            getFunctionsFromPackage = connection.prepareStatement(Constants.GET_FUNCTIONS_FROM_PACKAGE);
            getRecordsFromPackage = connection.prepareStatement(Constants.GET_RECORDS_FROM_PACKAGE);
            getObjectsFromPackage = connection.prepareStatement(Constants.GET_OBJECT_FROM_PACKAGE);
//             selectAllFunctions = connection.prepareStatement(Constants.SELECT_ALL_FUNCTIONS);
//             selectAllObjectsByType = connection.prepareStatement(Constants.SELECT_ALL_OBJECTS_BY_TYPE);
        } catch (SQLException e) {
            // TODO: Handle Properly
        }
    }

    public List<Integer> batchInsertBLangPackages(List<BPackageSymbolDTO> packageDTOs) throws SQLException {
        clearBatch(insertBLangPackage);
        for (BPackageSymbolDTO packageDTO : packageDTOs) {
            insertBLangPackage.setString(1, packageDTO.getPackageID().getName());
            insertBLangPackage.setString(2, packageDTO.getPackageID().getOrgName());
            insertBLangPackage.setString(3, packageDTO.getPackageID().getVersion());
            insertBLangPackage.addBatch();
        }
        insertBLangPackage.executeBatch();

        return this.getGeneratedKeys(insertBLangPackage.getGeneratedKeys());
    }

    public List<Integer> batchInsertBLangServices(List<BLangServiceDTO> bLangServiceDTOs) throws SQLException {
        clearBatch(insertBLangService);
        for (BLangServiceDTO bLangServiceDTO : bLangServiceDTOs) {
            insertBLangService.setInt(1, bLangServiceDTO.getPackageId());
            insertBLangService.setString(2, bLangServiceDTO.getName());
            insertBLangService.addBatch();
        }
        insertBLangService.executeBatch();

        return this.getGeneratedKeys(insertBLangService.getGeneratedKeys());
    }

    public List<Integer> batchInsertBLangResources(List<BLangResourceDTO> bLangResourceDTOs) throws SQLException {
        clearBatch(insertBLangResource);
        for (BLangResourceDTO bLangResourceDTO : bLangResourceDTOs) {
            insertBLangResource.setInt(1, bLangResourceDTO.getServiceId());
            insertBLangResource.setString(2, bLangResourceDTO.getName());
            insertBLangResource.addBatch();
        }
        insertBLangResource.executeBatch();

        return this.getGeneratedKeys(insertBLangResource.getGeneratedKeys());
    }

    public List<Integer> batchInsertBLangFunctions(List<BFunctionDTO> bFunctionDTOs)
         throws SQLException, IOException {
        clearBatch(insertBLangFunction);

        for (BFunctionDTO bFunctionDTO : bFunctionDTOs) {
            insertBLangFunction.setInt(1, bFunctionDTO.getPackageId());
            insertBLangFunction.setInt(2, bFunctionDTO.getObjectId());
            insertBLangFunction.setString(3, bFunctionDTO.getName());
            insertBLangFunction.setString(4, DTOUtil.completionItemToJSON(bFunctionDTO.getCompletionItem()));
            insertBLangFunction.addBatch();
        }
        insertBLangFunction.executeBatch();

        return this.getGeneratedKeys(insertBLangFunction.getGeneratedKeys());
    }

    public List<Integer> batchInsertBLangRecords(List<BRecordTypeSymbolDTO> recordDTOs) throws SQLException, IOException {
        clearBatch(insertBLangRecord);
        for (BRecordTypeSymbolDTO recordDTO : recordDTOs) {
            insertBLangRecord.setInt(1, recordDTO.getPackageId());
            insertBLangRecord.setString(2, recordDTO.getName());
            // TODO: Currently null
            insertBLangRecord.setString(3, "");
            insertBLangRecord.setString(4, DTOUtil.completionItemToJSON(recordDTO.getCompletionItem()));
            insertBLangRecord.addBatch();
        }

        insertBLangRecord.executeBatch();

        return this.getGeneratedKeys(insertBLangRecord.getGeneratedKeys());
    }

    public List<Integer> batchInsertBLangObjects(List<BObjectTypeSymbolDTO> objectDTOs) throws SQLException, IOException {
        clearBatch(insertBLangObject);
        for (BObjectTypeSymbolDTO objectDTO : objectDTOs) {
            insertBLangObject.setInt(1, objectDTO.getPackageId());
            insertBLangObject.setString(2, objectDTO.getName());
            // TODO: still null. handle properly
            insertBLangObject.setString(3, "");
            insertBLangObject.setInt(4, objectDTO.getType().getValue());
            insertBLangObject.setString(5, DTOUtil.completionItemToJSON(objectDTO.getCompletionItem()));
            insertBLangObject.addBatch();
        }

        insertBLangObject.executeBatch();
        return this.getGeneratedKeys(insertBLangObject.getGeneratedKeys());
    }

    /**
     * Update Action holderId entry of endpoint nodes
     * 
     * Note: endpoint IDs order and te actionHolder IDs holder order are equal.
     * @param endpoints
     * @param actionHolders
     * @return
     */
    public List<Integer> batchUpdateActionHolderId(List<Integer> endpoints, List<Integer> actionHolders)
            throws SQLException {
        clearBatch(updateActionHolderId);
        for (int i = 0; i < endpoints.size(); i++) {
            updateActionHolderId.setInt(1, actionHolders.get(i));
            updateActionHolderId.setInt(2, endpoints.get(i));
            updateActionHolderId.addBatch();
        }
        
        updateActionHolderId.executeBatch();
        
        return this.getGeneratedKeys(updateActionHolderId.getGeneratedKeys());
    }

    // Get Statements

    public ResultSet getPackageByOrgName(String orgName) throws SQLException {
        getBLangPackageByOrg.clearParameters();
        getBLangPackageByOrg.setString(1, orgName);
        return getBLangPackageByOrg.executeQuery();
    }
    
    public List<PackageFunctionDAO> getFunctionsFromPackage(String name, String orgName)
            throws SQLException {
        getFunctionsFromPackage.clearParameters();
        getFunctionsFromPackage.setString(1, name);
        getFunctionsFromPackage.setString(2, orgName);
        
        return DAOUtil.getPackageFunctionDAO(getFunctionsFromPackage.executeQuery());
    }
    
    public List<RecordDAO> getRecordsFromPackage(String name, String orgName) throws SQLException {
        getRecordsFromPackage.clearParameters();
        getRecordsFromPackage.setString(1, name);
        getRecordsFromPackage.setString(2, orgName);
        
        return DAOUtil.getRecordDAO(getRecordsFromPackage.executeQuery());
    }
    
    public List<ObjectDAO> getObjectsFromPackage(String name, String orgName) throws SQLException {
        getObjectsFromPackage.clearParameters();
        getObjectsFromPackage.setString(1, name);
        getObjectsFromPackage.setString(2, orgName);
        
        return DAOUtil.getObjectDAO(getObjectsFromPackage.executeQuery());
    }

    public ResultSet selectAllFunctions() throws SQLException {
        selectAllFunctions.clearParameters();
        return selectAllFunctions.executeQuery();
    }

    public ResultSet selectObjectsByType() throws SQLException {
        selectAllObjectsByType.clearParameters();
        //        selectAllObjectsByType.setInt(1, 3);
        return selectAllObjectsByType.executeQuery();
    }

    public Connection getConnection() {
        return connection;
    }

    private static void clearBatch(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.clearBatch();
    }

    private List<Integer> getGeneratedKeys(ResultSet resultSet) {
        List<Integer> generatedKeys = new ArrayList<>();
        try {
            while (resultSet.next()) {
                generatedKeys.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            // TODO: Handled Properly
        }
        return generatedKeys;
    }
}
