package org.ballerinalang.langserver.index;

import org.ballerinalang.langserver.index.dto.BLangFunctionDTO;
import org.ballerinalang.langserver.index.dto.BLangObjectDTO;
import org.ballerinalang.langserver.index.dto.BPackageSymbolDTO;
import org.ballerinalang.langserver.index.dto.BLangRecordDTO;
import org.ballerinalang.langserver.index.dto.BLangResourceDTO;
import org.ballerinalang.langserver.index.dto.BLangServiceDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
 
    private PreparedStatement selectAllFunctions;

    private PreparedStatement getFunctionsFromPackage;

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
//             updateActionHolderId = connection.prepareStatement(Constants.UPDATE_ACTION_HOLDER_ID,
//                     Statement.RETURN_GENERATED_KEYS);
//             getBLangPackageByOrg = connection.prepareStatement(Constants.GET_PACKAGE_BY_ORG);
//             getFunctionsFromPackage = connection.prepareStatement(Constants.GET_FUNCTIONS_FROM_PACKAGE);
             selectAllFunctions = connection.prepareStatement(Constants.SELECT_ALL_FUNCTIONS);
             selectAllObjectsByType = connection.prepareStatement(Constants.SELECT_ALL_OBJECTS_BY_TYPE);
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

        public List<Integer> batchInsertBLangFunctions(List<BLangFunctionDTO> bLangFunctionDTOs)
             throws SQLException, IOException {
            clearBatch(insertBLangFunction);

            for (BLangFunctionDTO bLangFunctionDTO : bLangFunctionDTOs) {
                insertBLangFunction.setInt(1, bLangFunctionDTO.getPackageId());
                insertBLangFunction.setInt(2, bLangFunctionDTO.getObjectId());
                insertBLangFunction.setString(3, bLangFunctionDTO.getName());
//                insertBLangFunction.setBinaryStream(4, serializeObject(bLangFunctionDTO.getCompletionItem()));
                insertBLangFunction.addBatch();
            }
            insertBLangFunction.executeBatch();

            return this.getGeneratedKeys(insertBLangFunction.getGeneratedKeys());
        }

        public List<Integer> batchInsertBLangRecords(List<BLangRecordDTO> recordDTOs) throws SQLException, IOException {
            clearBatch(insertBLangRecord);
            for (BLangRecordDTO recordDTO : recordDTOs) {
                insertBLangRecord.setInt(1, recordDTO.getPackageId());
                insertBLangRecord.setString(2, recordDTO.getName());
//                insertBLangRecord.setBinaryStream(3, serializeObject(recordDTO.getFields()));
                insertBLangRecord.addBatch();
            }

            insertBLangRecord.executeBatch();

            return this.getGeneratedKeys(insertBLangRecord.getGeneratedKeys());
        }

        public List<Integer> batchInsertBLangObjects(List<BLangObjectDTO> objectDTOs) throws SQLException, IOException {
            clearBatch(insertBLangObject);
            for (BLangObjectDTO objectDTO : objectDTOs) {
                insertBLangObject.setInt(1, objectDTO.getPackageId());
                insertBLangObject.setString(2, objectDTO.getName());
//                insertBLangObject.setBinaryStream(3, serializeObject(objectDTO.getFields()));
                insertBLangObject.setInt(4, objectDTO.getType().getValue());
                insertBLangObject.addBatch();
            }

            insertBLangObject.executeBatch();
            return this.getGeneratedKeys(insertBLangObject.getGeneratedKeys());
        }

    /**
     * 
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
    
    public ResultSet getFunctionsFromPackage(String name, String orgName, String version) throws SQLException {
        getFunctionsFromPackage.clearParameters();
        getFunctionsFromPackage.setString(1, name);
        getFunctionsFromPackage.setString(2, orgName);
        //        getFunctionsFromPackage.setString(3, version);
        
        return getFunctionsFromPackage.executeQuery();
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

    public Object getDeserializedObject(byte[] bytes, Class cls) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
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
