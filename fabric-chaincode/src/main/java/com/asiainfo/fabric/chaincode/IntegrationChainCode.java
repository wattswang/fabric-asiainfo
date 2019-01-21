/** 
* Project Name:fabric-chaincode 
* File Name:IntegrationChainCode.java 
* Package Name:com.asiainfo.fabric.chaincode 
* Date:2019年1月21日上午9:30:42 
* Copyright (c) 2019, wangchao9@asiainfo.com All Rights Reserved. 
**/
package com.asiainfo.fabric.chaincode;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;

/** 
* ClassName:IntegrationChainCode <br/> 
* Function: TODO ADD FUNCTION. <br/> 
* Reason: TODO ADD REASON. <br/> 
* Date: 2019年1月21日 上午9:30:42 <br/> 
* @author wangchao9 
* @version 
* @since JDK 1.8 
* @see 
*/
public class IntegrationChainCode extends ChaincodeBase{

	/* 
	*  
	*  
	* @param stub
	* @return 
	* @see org.hyperledger.fabric.shim.ChaincodeBase#init(org.hyperledger.fabric.shim.ChaincodeStub) 
	*/
	@Override
	public Response init(ChaincodeStub stub) {
		return null;
	}

	/* 
	*  
	*  
	* @param stub
	* @return 
	* @see org.hyperledger.fabric.shim.ChaincodeBase#invoke(org.hyperledger.fabric.shim.ChaincodeStub) 
	*/
	@Override
	public Response invoke(ChaincodeStub stub) {
		return null;
	}

}

