package org.wso2.ballerina.core.nativeimpl.connectors.data.sql;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;
import org.wso2.ballerina.core.nativeimpl.connectors.data.Constants;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.beans.DataSourceDefinition;
import org.wso2.carbon.datasource.core.impl.DataSourceServiceImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import javax.xml.bind.JAXBContext;

/**
 * Native RDBMS Connector.
 */
@BallerinaConnector(
        packageName = "ballerina.data.sql",
        connectorName = SQLConnector.CONNECTOR_NAME,
        args = {
                @Argument(name = "connectionParameters",
                          type = TypeEnum.MAP)
        })
@Component(
        name = "ballerina.data.connectors.sql",
        immediate = true,
        service = AbstractNativeConnector.class)
public class SQLConnector extends AbstractNativeConnector implements ServiceFactory {

    public static final String CONNECTOR_NAME = "Connector";

    private HikariDataSource hikariDataSource;

    @Override
    public boolean init(BValue[] bValueRefs) {
        if (bValueRefs != null && bValueRefs.length == 1) {
            BMap connProperties = (BMap) bValueRefs[0];
            hikariDataSource = getDataSource(connProperties);
        }
        return true;
    }

    @Override
    public AbstractNativeConnector getInstance() {
        return new SQLConnector();
    }

    @Override
    public String getPackageName() {
        return "ballerina.data.sql";
    }

    @Override
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration) {
        return new SQLConnector();
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object o) {
    }

    public Connection getSQLConnection() {
        Connection conn = null;
        if (hikariDataSource != null) {
            try {
                conn = hikariDataSource.getConnection();
            } catch (SQLException e) {
                throw new BallerinaException(
                        "Error in creating Connection in " + SQLConnector.CONNECTOR_NAME + ". " + e.getMessage());
            }
        }
        return conn;
    }

    private OMElement buildDataSourceConfig(BMap properties) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement definitionEl = fac.createOMElement(Constants.DATASOURCE_CONFIG_DEFINITION, null);
        definitionEl.addAttribute(Constants.DATASOURCE_CONFIG_TYPE, Constants.DATASOURCE_CONFIG_RDBMS, null);

        OMElement configurationEl = fac.createOMElement(Constants.DATASOURCE_CONFIG_CONFIGURATION, null);

        if (properties != null && !properties.isEmpty()) {
            Set<BString> keySet = properties.keySet();
            for (BString entry : keySet) {
                OMElement propEl = fac.createOMElement(entry.stringValue(), null);
                propEl.setText(properties.get(entry).stringValue());
                configurationEl.addChild(propEl);
            }
        }
        definitionEl.addChild(configurationEl);
        return definitionEl;
    }

    private HikariDataSource getDataSource(BMap properties) {
        OMElement dataSourceDefEl = buildDataSourceConfig(properties);
        HikariDataSource dataSourceObj = null;

        DataSourceDefinition dataSourceDefinition = new DataSourceDefinition();
        try {
            JAXBContext ctx = JAXBContext.newInstance(dataSourceDefinition.getClass());
            dataSourceDefinition = (DataSourceDefinition) ctx.createUnmarshaller()
                    .unmarshal(dataSourceDefEl.getXMLStreamReader());
            DataSourceService dataSourceService = new DataSourceServiceImpl();
            dataSourceObj = (HikariDataSource) dataSourceService.createDataSource(dataSourceDefinition);
        } catch (Throwable t) {
            throw new BallerinaException(
                    "Error in creating Hikari Data Source in " + SQLConnector.CONNECTOR_NAME + ". " + t.getMessage());
        }
        return dataSourceObj;
    }
}
