/** 
* Project Name:fabric-service 
* File Name:ProposalResponseDataUtils.java 
* Package Name:com.asiainfo.fabric.service.util 
* Date:2019年1月21日下午5:25:52 
* Copyright (c) 2019, wangchao9@asiainfo.com All Rights Reserved. 
**/
package com.asiainfo.fabric.service.util;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.asiainfo.fabric.service.ResponseStatus;


/**
 * ClassName:ProposalResponseDataUtils <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2019年1月21日 下午5:25:52 <br/>
 * 
 * @author wangchao9
 * @version
 * @since JDK 1.8
 * @see
 */
public class ProposalResponseUtils {
	
	private final static Logger logger = Logger.getLogger(ProposalResponseUtils.class);
	
	public static JSONObject toOrdererResponse(Collection<ProposalResponse> proposalResponses,Channel channel) throws InvalidArgumentException, UnsupportedEncodingException, TransactionException {
        JSONObject jsonObject = new JSONObject();
        ProposalResponse first = null;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        for (ProposalResponse response : proposalResponses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(proposalResponses);
        if (proposalConsistencySets.size() != 1) {
        	logger.error("Expected only one set of consistent proposal responses but got " + proposalConsistencySets.size());
        }
        if (failed.size() > 0) {
            for (ProposalResponse fail : failed) {
            	logger.error("Not enough endorsers for instantiate :" + successful.size() + "endorser failed with " + fail.getMessage() + ", on peer" + fail.getPeer());
            }
            first = failed.iterator().next();
            logger.error("Not enough endorsers for inspect:" + failed.size() + " endorser error: " + first.getMessage() + ". Was verified: "
                    + first.isVerified());
        }
        if (successful.size() > 0) {
        	logger.info("Successfully received transaction proposal responses.");
            ProposalResponse resp = proposalResponses.iterator().next();
            logger.debug("TransactionID: " + resp.getTransactionID());
            byte[] x = resp.getChaincodeActionResponsePayload();
            String resultAsString = null;
            if (x != null) {
                resultAsString = new String(x, "UTF-8");
            }
            logger.info("resultAsString = " + resultAsString);
            channel.sendTransaction(successful);
            jsonObject = ProposalResponseUtils.parseResult(resultAsString);
            jsonObject.put("code", ResponseStatus.SUCCESS);
            return jsonObject;
        } else {
            jsonObject.put("code", ResponseStatus.ERROR);
            jsonObject.put("error", null != first ? first.getMessage() : "error unknown");
            return jsonObject;
        }
    }
	
    public static JSONObject toPeerResponse(Collection<ProposalResponse> proposalResponses, boolean checkVerified) {
        JSONObject jsonObject = new JSONObject();
        for (ProposalResponse proposalResponse : proposalResponses) {
            if ((checkVerified && !proposalResponse.isVerified()) || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                String data = String.format("Failed install/query proposal from peer %s status: %s. Messages: %s. Was verified : %s",
                        proposalResponse.getPeer().getName(), proposalResponse.getStatus(), proposalResponse.getMessage(), proposalResponse.isVerified());
                logger.debug(data);
                jsonObject.put("code", ResponseStatus.ERROR);
                jsonObject.put("error", data);
            } else {
                String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                logger.debug("Install/Query payload from peer: " + proposalResponse.getPeer().getName());
                logger.debug("TransactionID: " + proposalResponse.getTransactionID());
                logger.debug("" + payload);
                jsonObject = parseResult(payload);
                jsonObject.put("code", ResponseStatus.SUCCESS);
                jsonObject.put("txid", proposalResponse.getTransactionID());
            }
        }
        return jsonObject;
    }
	
	
	public static JSONObject parseResult(String result) {
		JSONObject jsonObject = new JSONObject();
		int jsonVerify = isJSONValid(result);
		switch (jsonVerify) {
		case 0:
			jsonObject.put("data", result);
			break;
		case 1:
			jsonObject.put("data", JSONObject.parseObject(result));
			break;
		case 2:
			jsonObject.put("data", JSONObject.parseArray(result));
			break;
		}
		return jsonObject;
	}

	/**
	 * 判断字符串类型
	 *
	 * @param str
	 *            字符串
	 *
	 * @return 0-string；1-JsonObject；2、JsonArray
	 */
	public static int isJSONValid(String str) {
		try {
			JSONObject.parseObject(str);
			return 1;
		} catch (JSONException ex) {
			try {
				JSONObject.parseArray(str);
				return 2;
			} catch (JSONException ex1) {
				return 0;
			}
		}
	}

}
