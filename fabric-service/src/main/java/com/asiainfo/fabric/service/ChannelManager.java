/*
* Copyright @ 2019 com.asiainfo.com
* fabric-service 下午4:32:16
* All right reserved.
*
*/

package com.asiainfo.fabric.service;

import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;

import com.asiainfo.fabric.service.bean.OrgEntity;

public class ChannelManager {
	
	private static final Logger logger = Logger.getLogger(ChannelManager.class);
	
	public Channel createChannel(String name, HFClient client, OrgEntity sampleOrg) throws Exception {
		logger.info(String.format("start create channel, channel name : %s", name));
		return null;
	}
	
}

