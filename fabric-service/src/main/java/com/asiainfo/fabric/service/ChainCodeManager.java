/*
* Copyright @ 2019 com.asiainfo.com
* fabric-service 下午4:01:59
* All right reserved.
*
*/

package com.asiainfo.fabric.service;

import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.ChaincodeID;

import com.asiainfo.fabric.service.bean.ChainCodeEntity;

public class ChainCodeManager {
	
	private static Logger logger = Logger.getLogger(ChainCodeManager.class);
	
	public ChaincodeID getChainCodeId(ChainCodeEntity chainCode) {
		logger.info("get chaincodeId");
		
		/*String.format(" ", args);
		
		logger.assertLog(true, "");*/
		
		final ChaincodeID chainCodeId = ChaincodeID.newBuilder().setName(chainCode.getChainName())
				.setVersion(chainCode.getChainCodeVersion()).setPath(chainCode.getChainCodeVersion()).build();
		return chainCodeId;

	}

}
