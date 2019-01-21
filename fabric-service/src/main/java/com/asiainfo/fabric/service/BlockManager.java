/** 
* Project Name:fabric-service 
* File Name:BlockService.java 
* Package Name:com.asiainfo.fabric.service 
* Date:2019年1月15日下午3:15:57 
* Copyright (c) 2019, wangchao9@asiainfo.com All Rights Reserved. 
**/
package com.asiainfo.fabric.service;



import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;

import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeInfo;
import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType;
import org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.SDKUtils;
import org.hyperledger.fabric.sdk.TxReadWriteSetInfo;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.asiainfo.fabric.service.util.DateUtils;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * ClassName:BlockService <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2019年1月15日 下午3:15:57 <br/>
 * 
 * @author wangchao9
 * @version
 * @since JDK 1.8
 * @see
 */
public class BlockManager {
	
	private final static Logger logger = Logger.getLogger(BlockManager.class);

	FabricManager faricManager = FabricManager.getFaricManager();
	
	private static Channel channel;
	
	public BlockManager(){
		try {
			channel = faricManager.getChannel("mychannel", FabricConfig.getConfig().getOrgEntity("peerOrg1"));
		} catch (InvalidArgumentException | ProposalException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test() throws TransactionException, IOException, DecoderException{
		try {
			/*JSONObject queryBlockChainInfo = queryBlockChainInfo();
			System.out.println(queryBlockChainInfo.toString());*/
			String hash = "96d97aea1f5f645375387f88a8f4ec9025ce0131aabe4b9d550e5c96213fea77";
			JSONObject queryBlockByHash = queryBlockByHash(Hex.decodeHex(hash.toCharArray()));
			System.out.println(queryBlockByHash.toString());
		} catch (ProposalException | InvalidArgumentException e) {
			e.printStackTrace();
		}
	}

	public void getLastBlock() throws ProposalException, InvalidArgumentException {
		logger.info("get the last block");
		//Channel channel = faricManager.getChannel("mychannel", FabricConfig.getConfig().getOrgEntity("peerOrg1"));
		BlockchainInfo blockchainInfo = channel.queryBlockchainInfo();
		long height = blockchainInfo.getHeight();
		byte[] currentBlockHash = blockchainInfo.getCurrentBlockHash();
		byte[] previousBlockHash = blockchainInfo.getPreviousBlockHash();
		logger.info(String.format("the last block info -- height : %s , currentBlockHash : %s , previousBlockHash : %s", height,
				Hex.encodeHexString(currentBlockHash), Hex.encodeHexString(previousBlockHash)));
	}
	
    /**
     * 在指定频道内根据transactionID查询区块
     *
     * @param txID transactionID
     */
    JSONObject queryBlockByTransactionID(String txID) throws InvalidArgumentException, ProposalException, IOException, TransactionException {
        return execBlockInfo(channel.queryBlockByTransactionID(txID));
    }

    /**
     * 在指定频道内根据hash查询区块
     *
     * @param blockHash hash
     */
    JSONObject queryBlockByHash(byte[] blockHash) throws InvalidArgumentException, ProposalException, IOException, TransactionException {
        return execBlockInfo(channel.queryBlockByHash(blockHash));
    }

    /**
     * 在指定频道内根据区块高度查询区块
     *
     * @param blockNumber 区块高度
     */
    JSONObject queryBlockByNumber(long blockNumber) throws InvalidArgumentException, ProposalException, IOException, TransactionException {
        return execBlockInfo(channel.queryBlockByNumber(blockNumber));
    }
	
    /** 查询当前频道的链信息，包括链长度、当前最新区块hash以及当前最新区块的上一区块hash */
    JSONObject queryBlockChainInfo() throws InvalidArgumentException, ProposalException, TransactionException {
  /*      if (!channel.isInitialized()) {
            initChannel();
        }*/
        JSONObject blockchainInfo = new JSONObject();
        blockchainInfo.put("height", channel.queryBlockchainInfo().getHeight());
        blockchainInfo.put("currentBlockHash", Hex.encodeHexString(channel.queryBlockchainInfo().getCurrentBlockHash()));
        blockchainInfo.put("previousBlockHash", Hex.encodeHexString(channel.queryBlockchainInfo().getPreviousBlockHash()));
        return getSuccess(blockchainInfo);
    }
    
	
    /**
     * 解析区块信息对象
     *
     * @param blockInfo 区块信息对象
     */
    private JSONObject execBlockInfo(BlockInfo blockInfo) throws IOException, InvalidArgumentException {
        final long blockNumber = blockInfo.getBlockNumber();
        JSONObject blockJson = new JSONObject();
        blockJson.put("blockNumber", blockNumber);
        blockJson.put("dataHash", Hex.encodeHexString(blockInfo.getDataHash()));
        blockJson.put("previousHashID", Hex.encodeHexString(blockInfo.getPreviousHash()));
        blockJson.put("calculatedBlockHash", Hex.encodeHexString(SDKUtils.calculateBlockHash(FabricManager.client, blockNumber, blockInfo.getPreviousHash(), blockInfo.getDataHash())));
        blockJson.put("envelopeCount", blockInfo.getEnvelopeCount());

        logger.debug("blockNumber = " + blockNumber);
        logger.debug("data hash: " + Hex.encodeHexString(blockInfo.getDataHash()));
        logger.debug("previous hash id: " + Hex.encodeHexString(blockInfo.getPreviousHash()));
        logger.debug("calculated block hash is " + Hex.encodeHexString(SDKUtils.calculateBlockHash(FabricManager.client, blockNumber, blockInfo.getPreviousHash(), blockInfo.getDataHash())));
        logger.debug("block number " + blockNumber + " has " + blockInfo.getEnvelopeCount() + " envelope count:");

        blockJson.put("envelopes", getEnvelopeJsonArray(blockInfo, blockNumber));
        return getSuccess(blockJson);
    }
    
    /** 解析区块包 */
    private JSONArray getEnvelopeJsonArray(BlockInfo blockInfo, long blockNumber) throws UnsupportedEncodingException, InvalidProtocolBufferException {
        JSONArray envelopeJsonArray = new JSONArray();
        for (EnvelopeInfo info : blockInfo.getEnvelopeInfos()) {
            JSONObject envelopeJson = new JSONObject();
            envelopeJson.put("channelId", info.getChannelId());
            envelopeJson.put("transactionID", info.getTransactionID());
            envelopeJson.put("validationCode", info.getValidationCode());
            envelopeJson.put("timestamp", DateUtils.parseDateFormat(new Date(info.getTimestamp().getTime())));
            envelopeJson.put("type", info.getType());
            envelopeJson.put("createId", info.getCreator().getId());
            envelopeJson.put("createMSPID", info.getCreator().getMspid());
            envelopeJson.put("isValid", info.isValid());
            envelopeJson.put("nonce", Hex.encodeHexString(info.getNonce()));

            logger.debug("channelId = " + info.getChannelId());
            logger.debug("nonce = " + Hex.encodeHexString(info.getNonce()));
            logger.debug("createId = " + info.getCreator().getId());
            logger.debug("createMSPID = " + info.getCreator().getMspid());
            logger.debug("isValid = " + info.isValid());
            logger.debug("transactionID = " + info.getTransactionID());
            logger.debug("validationCode = " + info.getValidationCode());
            logger.debug("timestamp = " + DateUtils.parseDateFormat(new Date(info.getTimestamp().getTime())));
            logger.debug("type = " + info.getType());

            if (info.getType() == EnvelopeType.TRANSACTION_ENVELOPE) {
                TransactionEnvelopeInfo txeInfo = (TransactionEnvelopeInfo) info;
                JSONObject transactionEnvelopeInfoJson = new JSONObject();
                int txCount = txeInfo.getTransactionActionInfoCount();
                transactionEnvelopeInfoJson.put("txCount", txCount);
                transactionEnvelopeInfoJson.put("isValid", txeInfo.isValid());
                transactionEnvelopeInfoJson.put("validationCode", txeInfo.getValidationCode());

                logger.debug("Transaction number " + blockNumber + " has actions count = " + txCount);
                logger.debug("Transaction number " + blockNumber + " isValid = " + txeInfo.isValid());
                logger.debug("Transaction number " + blockNumber + " validation code = " + txeInfo.getValidationCode());

                transactionEnvelopeInfoJson.put("transactionActionInfoArray", getTransactionActionInfoJsonArray(txeInfo, txCount));
                envelopeJson.put("transactionEnvelopeInfo", transactionEnvelopeInfoJson);
            }
            envelopeJsonArray.add(envelopeJson);
        }
        return envelopeJsonArray;
    }

    /** 解析交易请求集合 */
    private JSONArray getTransactionActionInfoJsonArray(TransactionEnvelopeInfo txeInfo, int txCount) throws UnsupportedEncodingException, InvalidProtocolBufferException {
        JSONArray transactionActionInfoJsonArray = new JSONArray();
        for (int i = 0; i < txCount; i++) {
            TransactionEnvelopeInfo.TransactionActionInfo txInfo = txeInfo.getTransactionActionInfo(i);
            int endorsementsCount = txInfo.getEndorsementsCount();
            int chaincodeInputArgsCount = txInfo.getChaincodeInputArgsCount();
            JSONObject transactionActionInfoJson = new JSONObject();
            transactionActionInfoJson.put("responseStatus", txInfo.getResponseStatus());
            transactionActionInfoJson.put("responseMessageString", printableString(new String(txInfo.getResponseMessageBytes(), "UTF-8")));
            transactionActionInfoJson.put("endorsementsCount", endorsementsCount);
            transactionActionInfoJson.put("chaincodeInputArgsCount", chaincodeInputArgsCount);
            transactionActionInfoJson.put("status", txInfo.getProposalResponseStatus());
            transactionActionInfoJson.put("payload", printableString(new String(txInfo.getProposalResponsePayload(), "UTF-8")));

            logger.debug("Transaction action " + i + " has response status " + txInfo.getResponseStatus());
            logger.debug("Transaction action " + i + " has response message bytes as string: " + printableString(new String(txInfo.getResponseMessageBytes(), "UTF-8")));
            logger.debug("Transaction action " + i + " has endorsements " + endorsementsCount);

            transactionActionInfoJson.put("endorserInfoArray", getEndorserInfoJsonArray(txInfo, endorsementsCount));

            logger.debug("Transaction action " + i + " has " + chaincodeInputArgsCount + " chaincode input arguments");

            transactionActionInfoJson.put("argArray", getArgJSONArray(i, txInfo, chaincodeInputArgsCount));

            logger.debug("Transaction action " + i + " proposal response status: " + txInfo.getProposalResponseStatus());
            logger.debug("Transaction action " + i + " proposal response payload: " + printableString(new String(txInfo.getProposalResponsePayload())));

            TxReadWriteSetInfo rwsetInfo = txInfo.getTxReadWriteSet();
            JSONObject rwsetInfoJson = new JSONObject();
            if (null != rwsetInfo) {
                int nsRWsetCount = rwsetInfo.getNsRwsetCount();
                rwsetInfoJson.put("nsRWsetCount", nsRWsetCount);
                logger.debug("Transaction action " + i + " has " + nsRWsetCount + " name space read write sets");
                rwsetInfoJson.put("nsRwsetInfoArray", getNsRwsetInfoJsonArray(rwsetInfo));
            }
            transactionActionInfoJson.put("rwsetInfo", rwsetInfoJson);
            transactionActionInfoJsonArray.add(transactionActionInfoJson);
        }
        return transactionActionInfoJsonArray;
    }

    /** 解析参数 */
    private JSONArray getArgJSONArray(int i, TransactionEnvelopeInfo.TransactionActionInfo txInfo, int chaincodeInputArgsCount) throws UnsupportedEncodingException {
        JSONArray argJsonArray = new JSONArray();
        for (int z = 0; z < chaincodeInputArgsCount; ++z) {
            argJsonArray.add(printableString(new String(txInfo.getChaincodeInputArgs(z), "UTF-8")));
            logger.debug("Transaction action " + i + " has chaincode input argument " + z + "is: " + printableString(new String(txInfo.getChaincodeInputArgs(z), "UTF-8")));
        }
        return argJsonArray;
    }

    /** 解析背书信息 */
    private JSONArray getEndorserInfoJsonArray(TransactionEnvelopeInfo.TransactionActionInfo txInfo, int endorsementsCount) {
        JSONArray endorserInfoJsonArray = new JSONArray();
        for (int n = 0; n < endorsementsCount; ++n) {
            BlockInfo.EndorserInfo endorserInfo = txInfo.getEndorsementInfo(n);
            String signature = Hex.encodeHexString(endorserInfo.getSignature());
            String id = endorserInfo.getId();
            String mspId = endorserInfo.getMspid();
            JSONObject endorserInfoJson = new JSONObject();
            endorserInfoJson.put("signature", signature);
            endorserInfoJson.put("id", id);
            endorserInfoJson.put("mspId", mspId);

            logger.debug("Endorser " + n + " signature: " + signature);
            logger.debug("Endorser " + n + " id: " + id);
            logger.debug("Endorser " + n + " mspId: " + mspId);
            endorserInfoJsonArray.add(endorserInfoJson);
        }
        return endorserInfoJsonArray;
    }

    /** 解析读写集集合 */
    private JSONArray getNsRwsetInfoJsonArray(TxReadWriteSetInfo rwsetInfo) throws InvalidProtocolBufferException, UnsupportedEncodingException {
        JSONArray nsRwsetInfoJsonArray = new JSONArray();
        for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : rwsetInfo.getNsRwsetInfos()) {
            final String namespace = nsRwsetInfo.getNamespace();
            KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();
            JSONObject nsRwsetInfoJson = new JSONObject();

            nsRwsetInfoJson.put("readSet", getReadSetJSONArray(rws, namespace));
            nsRwsetInfoJson.put("writeSet", getWriteSetJSONArray(rws, namespace));
            nsRwsetInfoJsonArray.add(nsRwsetInfoJson);
        }
        return nsRwsetInfoJsonArray;
    }

    /** 解析读集 */
    private JSONArray getReadSetJSONArray(KvRwset.KVRWSet rws, String namespace) {
        JSONArray readJsonArray = new JSONArray();
        int rs = -1;
        for (KvRwset.KVRead readList : rws.getReadsList()) {
            rs++;
            String key = readList.getKey();
            long readVersionBlockNum = readList.getVersion().getBlockNum();
            long readVersionTxNum = readList.getVersion().getTxNum();
            JSONObject readInfoJson = new JSONObject();
            readInfoJson.put("namespace", namespace);
            readInfoJson.put("readSetIndex", rs);
            readInfoJson.put("key", key);
            readInfoJson.put("readVersionBlockNum", readVersionBlockNum);
            readInfoJson.put("readVersionTxNum", readVersionTxNum);
            readInfoJson.put("chaincode_version", String.format("[%s : %s]", readVersionBlockNum, readVersionTxNum));
            readJsonArray.add(readInfoJson);
            logger.debug("Namespace " + namespace + " read set " + rs + " key " + key + " version [" + readVersionBlockNum + " : " + readVersionTxNum + "]");
        }
        return readJsonArray;
    }

    /** 解析写集 */
    private JSONArray getWriteSetJSONArray(KvRwset.KVRWSet rws, String namespace) throws UnsupportedEncodingException {
        JSONArray writeJsonArray = new JSONArray();
        int rs = -1;
        for (KvRwset.KVWrite writeList : rws.getWritesList()) {
            rs++;
            String key = writeList.getKey();
            String valAsString = printableString(new String(writeList.getValue().toByteArray(), "UTF-8"));
            JSONObject writeInfoJson = new JSONObject();
            writeInfoJson.put("namespace", namespace);
            writeInfoJson.put("writeSetIndex", rs);
            writeInfoJson.put("key", key);
            writeInfoJson.put("value", valAsString);
            logger.debug("Namespace " + namespace + " write set " + rs + " key " + key + " has value " + valAsString);
            writeJsonArray.add(writeInfoJson);
        }
        return writeJsonArray;
    }


    
    private JSONObject getSuccess(JSON json) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", ResponseStatus.SUCCESS);
        jsonObject.put("data", json);
        return jsonObject;
    }

/*    private JSONObject getSuccessFromString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", ResponseStatus.SUCCESS);
        jsonObject.put("data", JSON.parse("peer join channel success"));
        return jsonObject;
    }

    private JSONObject getFailFromString(String data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", ResponseStatus.ERROR);
        jsonObject.put("data", data);
        return jsonObject;
    }*/
    
    private String printableString(final String string) {
        int maxLogStringLength = 64;
        if (string == null || string.length() == 0) {
            return string;
        }
        String ret = string.replaceAll("[^\\p{Print}]", "?");
        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ? "..." : "");
        return ret;
    }
}
