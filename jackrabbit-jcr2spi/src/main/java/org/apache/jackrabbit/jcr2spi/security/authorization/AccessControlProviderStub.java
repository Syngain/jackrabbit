package org.apache.jackrabbit.jcr2spi.security.authorization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.jackrabbit.jcr2spi.config.RepositoryConfig;
import org.apache.jackrabbit.spi.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stub class that provide clients with access to a concrete AccessControlProvider implementation.
 * TODO: Explain the way the concrete provider is located, loaded and instantiated.
 * 
 */
public class AccessControlProviderStub {

    private static Logger log = LoggerFactory.getLogger(AccessControlProviderStub.class);

    /**
     * The class property parameter determines the {@link AccessControlProvider}
     * to load and instantiate. This is a fall-back parameter if the SYS_PROP_AC_PROVIDER_IMPL is not set.
     */
    private static final String ACCESS_CONTROL_PROVIDER_PROPERTIES = "accessControlProvider.properties";

    /**
     * Key look-up.
     */
    private static final String PROPERTY_ACCESSCONTROL_PROVIDER_CLASS = "org.apache.jackrabbit.jcr2spi.AccessControlProvider.class";

    /**
     * Avoid instantiation.
     */
    private AccessControlProviderStub() {}

    /**
     * Instantiates and returns a concrete AccessControlProvider implementation.
     * @param service     The repository service.
     * @return
     * @throws RepositoryException
     */
    public static AccessControlProvider newInstance(RepositoryConfig config, RepositoryService service) throws RepositoryException {
        String className = getProviderClass(config);
        if (className != null) {
            try {
                Class<?> acProviderClass = Class.forName(className);
                if (acProviderClass.isAssignableFrom(AccessControlProvider.class)) {
                    AccessControlProvider acProvider = (AccessControlProvider) acProviderClass.newInstance();
                    acProvider.init(config, service);
                    return acProvider;
                } else {
                    throw new RepositoryException("Fail to create AccessControlProvider from configuration.");
                }
            } catch (Exception e) {
                throw new RepositoryException("Fail to create AccessControlProvider from configuration.");
            }
        }

        // ac not supported in this setup.
        throw new UnsupportedRepositoryOperationException("Access control is not supported");
    }
    
    private static String getProviderClass(RepositoryConfig config) throws RepositoryException {
        Properties prop = new Properties();
        String providerImplProp = config.getConfiguration(ACCESS_CONTROL_PROVIDER_PROPERTIES, null);
        try {
            if (providerImplProp == null) {
                // not configured try to load as resource
                InputStream is = AccessControlProviderStub.class.getResourceAsStream(ACCESS_CONTROL_PROVIDER_PROPERTIES);
                if (is != null) {
                    prop.load(is);
                } else {
                    log.debug("Fail to locate the access control provider properties file.");
                    return null;
                }
            } else {
                File file = new File(providerImplProp);
                if (file.exists()) { // check that path actually exist.
                    prop.load(new FileInputStream(file));
                } else {
                    log.debug("Fail to locate the access control provider properties file.");
                }
            }

            // loads the concrete class to instantiate.
            String  className = prop.getProperty(PROPERTY_ACCESSCONTROL_PROVIDER_CLASS, "");
            if (!className.isEmpty()) {
                return className;
            } else {
                log.debug("Missing AccessControlProvider configuration.");
                return null;
            }
        } catch (IOException e) {
            throw new RepositoryException("Fail to load AccessControlProvider configuration.");
        }
    }
}