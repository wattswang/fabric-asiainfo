/*
* Copyright @ 2019 com.asiainfo.com
* fabric-service 下午4:23:05
* All right reserved.
*
*/

package com.asiainfo.fabric.service.bean;

import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;

public class ChainCodeEventCaptureEntity {
	
	private String handle;
	private BlockEvent blockEvent;
	private ChaincodeEvent chaincodeEvent;

    void ChaincodeEventCapture(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
        this.handle = handle;
        this.blockEvent = blockEvent;
        this.chaincodeEvent = chaincodeEvent;
    }

	public String getHandle() {
	
		return handle;
	}

	public void setHandle(String handle) {
	
		this.handle = handle;
	}

	public BlockEvent getBlockEvent() {
	
		return blockEvent;
	}

	public void setBlockEvent(BlockEvent blockEvent) {
	
		this.blockEvent = blockEvent;
	}

	public ChaincodeEvent getChaincodeEvent() {
	
		return chaincodeEvent;
	}

	public void setChaincodeEvent(ChaincodeEvent chaincodeEvent) {
	
		this.chaincodeEvent = chaincodeEvent;
	}
    
    
	
}

