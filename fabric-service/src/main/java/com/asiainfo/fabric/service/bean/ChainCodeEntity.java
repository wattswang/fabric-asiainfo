/*
* Copyright @ 2019 com.asiainfo.com
* fabric-service 下午3:34:33
* All right reserved.
*
*/

package com.asiainfo.fabric.service.bean;

import org.hyperledger.fabric.sdk.ChaincodeID;

public class ChainCodeEntity{
	
	private String chainCodePath;  //链码路径
	
	private String chainName;	//链码名称
	
	private String chainCodeVersion;	//链码版本
	
	private String chainCodeLang;	//链码语言
	
	public String getChainCodePath() {
		return chainCodePath;
	}

	public ChaincodeID getChaincodeId() {
		return ChaincodeID.newBuilder().setName(chainName)
				.setVersion(chainCodeVersion).setPath(chainCodePath).build();
	}

	public void setChainCodePath(String chainCodePath) {
	
		this.chainCodePath = chainCodePath;
	}

	public String getChainName() {
	
		return chainName;
	}

	public void setChainName(String chainName) {
	
		this.chainName = chainName;
	}

	public String getChainCodeVersion() {
	
		return chainCodeVersion;
	}

	public void setChainCodeVersion(String chainCodeVersion) {
	
		this.chainCodeVersion = chainCodeVersion;
	}

	public String getChainCodeLang() {
	
		return chainCodeLang;
	}

	public void setChainCodeLang(String chainCodeLang) {
	
		this.chainCodeLang = chainCodeLang;
	}
	
	
}

