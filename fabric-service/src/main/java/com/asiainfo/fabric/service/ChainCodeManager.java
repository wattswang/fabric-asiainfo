/*
* Copyright @ 2019 com.asiainfo.com
* fabric-service 下午4:01:59
* All right reserved.
*
*/

package com.asiainfo.fabric.service;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;
import com.asiainfo.fabric.service.bean.ChainCodeEntity;
import com.asiainfo.fabric.service.util.ProposalResponseUtils;

public class ChainCodeManager {
	
	//private static Logger logger = Logger.getLogger(ChainCodeManager.class);
	
	FabricManager faricManager = FabricManager.getFaricManager();
	
	public ChaincodeID getChainCodeId(ChainCodeEntity chainCode) {
		return ChaincodeID.newBuilder().setName(chainCode.getChainName())
				.setVersion(chainCode.getChainCodeVersion()).setPath(chainCode.getChainCodeVersion()).build();
	}
	
	
	/**
	 * 
	 * query:通过链码查询数据. <br/> 
	 * 
	 * @author wangchao9
	 * @param queryReq
	 * @return
	 * @throws Exception 
	 * @since JDK 1.8
	 */
	public JSONObject query(ChaincodeRequest queryReq) throws Exception {
		Channel channel = faricManager.getChannel("mychannel", FabricConfig.getConfig().getOrgEntity("peerOrg1"));
		QueryByChaincodeRequest request = FabricManager.client.newQueryProposalRequest();
		request.setChaincodeID(ChaincodeID.newBuilder().setName("integration").build());
		request.setFcn(queryReq.getFuc());
		request.setProposalWaitTime(200000);
		request.setArgs(queryReq.getArgs());
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
        request.setTransientMap(tm2);
        Collection<ProposalResponse> proposalResponses = channel.queryByChaincode(request, channel.getPeers());
        return ProposalResponseUtils.toPeerResponse(proposalResponses, true);
	}
	
	/**
	 * 
	 * invoke:链码执行<br/> 
	 * 
	 * @author wangchao9
	 * @param paramReq
	 * @throws Exception 
	 * @since JDK 1.8
	 */
	public JSONObject invoke(ChaincodeRequest inovkeReq) throws Exception {
		Channel channel = faricManager.getChannel("mychannel", FabricConfig.getConfig().getOrgEntity("peerOrg1"));
		TransactionProposalRequest request = FabricManager.client.newTransactionProposalRequest();
		request.setChaincodeID(ChaincodeID.newBuilder().setName("integration").build());
		request.setFcn(inovkeReq.getFuc());
		request.setProposalWaitTime(200000);
		request.setArgs(inovkeReq.getArgs());
        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("HyperLedgerFabric", "TransactionProposalRequest:JavaSDK".getBytes(UTF_8));
        tm2.put("method", "TransactionProposalRequest".getBytes(UTF_8));
        tm2.put("result", ":)".getBytes(UTF_8));
        request.setTransientMap(tm2);
        Collection<ProposalResponse> sendTransactionProposal = channel.sendTransactionProposal(request);
        for (ProposalResponse proposalResponse : sendTransactionProposal) {
        	String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
        	System.out.println(payload);
		}
        return ProposalResponseUtils.toOrdererResponse(sendTransactionProposal,channel);
	}
	
	
	@Test
	public void queryTest(){
		ChaincodeRequest request = new ChaincodeRequest();
		request.setFuc(ChaincodeRequest.ChaincodeExecOp.QUERYHISTORY);
		//request.setArgs("10000","15189839843","陈三");
		request.setArgs("344ede8555424d28a32887f820028ff5","100","ESLFUKFS");
		try {
			//JSONObject str = invoke(request);
			JSONObject str = query(request);
			System.out.println(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
