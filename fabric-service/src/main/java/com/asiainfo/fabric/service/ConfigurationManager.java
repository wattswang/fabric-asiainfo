/*
* Copyright @ 2019 com.asiainfo.com
* fabric-service 上午10:20:54
* All right reserved.
*
*/

package com.asiainfo.fabric.service;

import com.asiainfo.fabric.service.util.FabricUtils;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.openssl.PEMWriter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAInfo;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import com.asiainfo.fabric.service.bean.OrgEntity;
import com.asiainfo.fabric.service.bean.UserEntity;

public class ConfigurationManager {
	
	private static final Log logger = LogFactory.getLog(ConfigurationManager.class);
	
	private final FabricConfigHelper configHelper = new FabricConfigHelper();
	
	private static final FabricConfig fabricConfig = FabricConfig.getConfig();
	
	StoreManager sampleStore = null;
	
	File sampleStoreFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");
	
	private Collection<OrgEntity> orgs;
	
    public void checkConfig(String name) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, MalformedURLException, org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException {
    	logger.info(String.format("\n\n\nRUNNING: %s.\n", name));

    	FabricUtils.resetConfig();
        configHelper.customizeConfig();

        orgs = fabricConfig.getIntegrationTestsSampleOrgs();
        //Set up hfca for each sample org
        for (OrgEntity sampleOrg : orgs) {
            String caName = sampleOrg.getCAName(); //Try one of each name and no name.
            if (caName != null && !caName.isEmpty()) {
                sampleOrg.setCAClient(HFCAClient.createNewInstance(caName, sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
            } else {
                sampleOrg.setCAClient(HFCAClient.createNewInstance(sampleOrg.getCALocation(), sampleOrg.getCAProperties()));
            }
        }
    }
    
    public void setup() throws Exception {
        //Persistence is not part of SDK. Sample file store is for demonstration purposes only!
        //   MUST be replaced with more robust application implementation  (Database, LDAP)

        if (sampleStoreFile.exists()) { //For testing start fresh
            sampleStoreFile.delete();
        }
        sampleStore = new StoreManager(sampleStoreFile);
        enrollUsersSetup(sampleStore); //This enrolls users with fabric ca and setups sample store to get users later.
        //runFabricTest(sampleStore); //Runs Fabric tests with constructing channels, joining peers, exercising chaincode

    }
    
    public static void main(String[] args) throws Exception {
    	ConfigurationManager configurationManager = new ConfigurationManager();
    	configurationManager.checkConfig("test");
    	configurationManager.setup();
	}
    
    
    Map<String, Properties> clientTLSProperties = new HashMap<>();
    static final String TEST_ADMIN_NAME = "admin";
    static String testUser1 = "user1";
    
    public void enrollUsersSetup(StoreManager sampleStore) throws Exception {
        ////////////////////////////

        for (OrgEntity sampleOrg : orgs) {

            HFCAClient ca = sampleOrg.getCAClient();

            final String orgName = sampleOrg.getName();
            final String mspid = sampleOrg.getMSPID();
            ca.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

            if (fabricConfig.isRunningFabricTLS()) {
                //This shows how to get a client TLS certificate from Fabric CA
                // we will use one client TLS certificate for orderer peers etc.
                final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
                enrollmentRequestTLS.addHost("localhost");
                enrollmentRequestTLS.setProfile("tls");
                final Enrollment enroll = ca.enroll("admin", "adminpw", enrollmentRequestTLS);
                final String tlsCertPEM = enroll.getCert();
                final String tlsKeyPEM = getPEMStringFromPrivateKey(enroll.getKey());

                final Properties tlsProperties = new Properties();

                tlsProperties.put("clientKeyBytes", tlsKeyPEM.getBytes(UTF_8));
                tlsProperties.put("clientCertBytes", tlsCertPEM.getBytes(UTF_8));
                clientTLSProperties.put(sampleOrg.getName(), tlsProperties);
                //Save in samplestore for follow on tests.
                sampleStore.storeClientPEMTLCertificate(sampleOrg, tlsCertPEM);
                sampleStore.storeClientPEMTLSKey(sampleOrg, tlsKeyPEM);
            }

            HFCAInfo info = ca.info(); //just check if we connect at all.
            assertNotNull(info);
            String infoName = info.getCAName();
            if (infoName != null && !infoName.isEmpty()) {
                assertEquals(ca.getCAName(), infoName);
            }

            UserEntity admin = sampleStore.getMember(TEST_ADMIN_NAME, orgName);
            if (!admin.isEnrolled()) {  //Preregistered admin only needs to be enrolled with Fabric caClient.
                admin.setEnrollment(ca.enroll(admin.getName(), "adminpw"));
                admin.setMspId(mspid);
            }

            UserEntity user = sampleStore.getMember(testUser1, sampleOrg.getName());
            if (!user.isRegistered()) {  // users need to be registered AND enrolled
                RegistrationRequest rr = new RegistrationRequest(user.getName(), "org1.department1");
                user.setEnrollmentSecret(ca.register(rr, admin));
            }
            if (!user.isEnrolled()) {
                user.setEnrollment(ca.enroll(user.getName(), user.getEnrollmentSecret()));
                user.setMspId(mspid);
            }

            final String sampleOrgName = sampleOrg.getName();
            final String sampleOrgDomainName = sampleOrg.getDomainName();

            UserEntity peerOrgAdmin = sampleStore.getMember(sampleOrgName + "Admin", sampleOrgName, sampleOrg.getMSPID(),
                    Util.findFileSk(Paths.get(fabricConfig.getTestChannelPath(), "crypto-config/peerOrganizations/",
                            sampleOrgDomainName, format("/users/Admin@%s/msp/keystore", sampleOrgDomainName)).toFile()),
                    Paths.get(fabricConfig.getTestChannelPath(), "crypto-config/peerOrganizations/", sampleOrgDomainName,
                            format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem", sampleOrgDomainName, sampleOrgDomainName)).toFile());
            sampleOrg.setPeerAdmin(peerOrgAdmin); //A special user that can create channels, join peers and install chaincode

            sampleOrg.addUser(user);
            sampleOrg.setAdmin(admin); // The admin of this org --
        }
    }
    
    
    static String getPEMStringFromPrivateKey(PrivateKey privateKey) throws IOException {
        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);

        pemWriter.writeObject(privateKey);

        pemWriter.close();

        return pemStrWriter.toString();
    }
	
}

