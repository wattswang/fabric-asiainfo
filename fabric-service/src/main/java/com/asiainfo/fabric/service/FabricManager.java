/** 
* Project Name:fabric-service 
* File Name:FabricManager.java 
* Package Name:com.asiainfo.fabric.service 
* Date:2019年1月15日下午2:50:22 
* Copyright (c) 2019, wangchao9@asiainfo.com All Rights Reserved. 
**/
package com.asiainfo.fabric.service;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.asiainfo.fabric.service.bean.OrgEntity;
import com.asiainfo.fabric.service.bean.UserEntity;
import com.asiainfo.fabric.service.util.FabricUtils;

/**
 * ClassName:FabricManager <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2019年1月15日 下午2:50:22 <br/>
 * 
 * @author wangchao9
 * @version
 * @since JDK 1.8
 * @see
 */
public class FabricManager {

	private static final FabricConfig fabricConfig = FabricConfig.getConfig();
	private final FabricConfigHelper configHelper = new FabricConfigHelper();

	private static final String CRYPTO_CONFIG_PATH = "src/main/resources";

	static final String ADMIN_NAME = "admin";
	static final String USER_NAME = "user";

	private Collection<OrgEntity> orgEntitys;
	public static HFClient client = null;
	
	/**
	 * channles
	 */
	private static final Map<String,Channel> channels = new HashMap<String,Channel>();

	File sampleStoreFile = new File(System.getProperty("java.io.tmpdir") + "/HFCSampletest.properties");

	StoreManager storeManager = null;
	
	private static FabricManager config;

	private FabricManager() throws Exception{
		FabricUtils.resetConfig();
		configHelper.customizeConfig();
		orgEntitys = fabricConfig.getOrgEntitys();
		if (sampleStoreFile.exists()) { // For testing start fresh
			sampleStoreFile.delete();
		}
		storeManager = new StoreManager(sampleStoreFile);
		enrollUsersSetup(storeManager);
		OrgEntity peerOrgEntity = fabricConfig.getOrgEntity("peerOrg1");
		client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(peerOrgEntity.getPeerAdmin());
	}
	
	public static FabricManager getFaricManager(){
		if(null == config){
			try {
				config = new FabricManager();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return config;
	}

	/**
	 * 
	 * getChannel:(这里用一句话描述这个方法的作用). <br/>
	 * 
	 * @author wangchao9
	 * @param channelName
	 * @param client
	 * @param orgEntity
	 * @return
	 * @throws InvalidArgumentException
	 * @throws ProposalException 
	 * @since JDK 1.8
	 */
	Channel getChannel(String channelName, OrgEntity orgEntity) throws InvalidArgumentException, ProposalException {
		Channel channel = channels.get(channelName);
		if(null != channel){
			return channel;
		}
		return createChannel(channelName,orgEntity);
	}

	Channel createChannel(String channelName, OrgEntity orgEntity) throws InvalidArgumentException, ProposalException {
		Collection<Orderer> orderers = new LinkedList<>();
		Set<String> ordererNames = orgEntity.getOrdererNames();
		for (String orderName : ordererNames) {
			Properties ordererProperties = fabricConfig.getOrdererProperties(orderName);
			ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
					new Object[] { 8L, TimeUnit.MINUTES });
			ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
					new Object[] { 16L, TimeUnit.SECONDS });
			ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[] { true });
			orderers.add(client.newOrderer(orderName, orgEntity.getOrdererLocation(orderName), ordererProperties));
		}
		Orderer orderer = orderers.iterator().next();
		orderers.remove(orderer);
		Channel newChannel = client.newChannel(channelName);
		newChannel.addOrderer(orderer);
		// newChannel.joinPeer(null);
		Set<String> peerNames = orgEntity.getPeerNames();
		for (String peerName : peerNames) {
			String peerLocation = orgEntity.getPeerLocation(peerName);
			Properties peerProperties = fabricConfig.getPeerProperties(peerName);
			if (peerProperties == null) {
				peerProperties = new Properties();
			}
			peerProperties.put("grpc.NettyChannelBuilderOption.maxInboundMessageSize", 9000000);
			Peer peer = client.newPeer(peerName, peerLocation, peerProperties);
			newChannel.addPeer(peer);
			//newChannel.addPeer(peer, PeerOptions.createPeerOptions().setPeerRoles(EnumSet.of(PeerRole.LEDGER_QUERY,PeerRole.CHAINCODE_QUERY)));
			//newChannel.joinPeer(peer, PeerOptions.createPeerOptions().setPeerRoles(EnumSet.of(PeerRole.CHAINCODE_QUERY))); //Default is all roles.

		}

		for (Orderer _orderer : orderers) { // add remaining orderers if any.
			newChannel.addOrderer(_orderer);
		}
		for (String eventHubName : orgEntity.getEventHubNames()) {

			final Properties eventHubProperties = fabricConfig.getEventHubProperties(eventHubName);

			eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime",
					new Object[] { 5L, TimeUnit.MINUTES });
			eventHubProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout",
					new Object[] { 8L, TimeUnit.SECONDS });

			EventHub eventHub = client.newEventHub(eventHubName, orgEntity.getEventHubLocation(eventHubName),
					eventHubProperties);
			newChannel.addEventHub(eventHub);
		}
		try {
			return newChannel.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void enrollUsersSetup(StoreManager storeManager)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {
		for (OrgEntity orgEntity : orgEntitys) {
			final String orgName = orgEntity.getName();
			final String mspid = orgEntity.getMSPID();

			UserEntity admin = storeManager.getMember(ADMIN_NAME, orgName);
			UserEntity user = storeManager.getMember(USER_NAME, orgName);
			File privateKeyFile = Util.findFileSk(
					Paths.get(CRYPTO_CONFIG_PATH, "crypto-config/peerOrganizations/", orgEntity.getDomainName(),
							String.format("/users/Admin@%s/msp/keystore", orgEntity.getDomainName())).toFile());
			File certificateFile = Paths.get(CRYPTO_CONFIG_PATH, "crypto-config/peerOrganizations/",
					orgEntity.getDomainName(), format("/users/Admin@%s/msp/signcerts/Admin@%s-cert.pem",
							orgEntity.getDomainName(), orgEntity.getDomainName()))
					.toFile();
			UserEntity peerAdmin = storeManager.getMember(orgEntity.getName() + "Admin", orgEntity.getName(), mspid,
					privateKeyFile, certificateFile);
			orgEntity.setPeerAdmin(peerAdmin);
			orgEntity.addUser(user);
			orgEntity.setAdmin(admin);
		}
	}

}
