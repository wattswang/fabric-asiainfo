/** 
* Project Name:fabric-chaincode 
* File Name:SimpleChaincode.java 
* Package Name:com.asiainfo.fabric.chaincode 
* Date:2019年1月15日下午4:46:30 
* Copyright (c) 2019, wangchao9@asiainfo.com All Rights Reserved. 
**/
package com.asiainfo.fabric.chaincode;

import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import io.netty.handler.ssl.OpenSsl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

/**
 * ClassName:SimpleChaincode <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON. <br/>
 * Date: 2019年1月15日 下午4:46:30 <br/>
 * 
 * @author wangchao9
 * @version
 * @since JDK 1.8
 * @see
 */
public class SimpleChaincode extends ChaincodeBase {

    private static Log _logger = LogFactory.getLog(SimpleChaincode.class);
    
    private static final String INIT_TYPE = "INIT";
    private static final String TRANSACTION_TYPE = "TRANSACTION";
    private static final String DESTROY_TYPE = "DESTROY";
    

	@Override
	public Response init(ChaincodeStub stub) {
		_logger.info("Init java simple chaincode");
		String func = stub.getFunction();
		if (!func.equals("init")) {
			return newErrorResponse("function other than init is not supported");
		}
		String key = "JDd44ca6510aea4e60a4634b6b2974f2";
		String amount = "10000000000";
		/*初始化数据*/
		JsonObject valueJson = new JsonObject();
		valueJson.addProperty("amount", amount);
		valueJson.addProperty("txn", INIT_TYPE);
		valueJson.addProperty("currentKey", key);
		valueJson.addProperty("previousKey", "system");
		valueJson.addProperty("toKey", "");
		valueJson.addProperty("remark", String.format("init the data, key : %S ,amout : %s",key,amount));
		JsonObject ownerJson = new JsonObject();
		ownerJson.addProperty("phone", "10000086");
		ownerJson.addProperty("name", "京东");
		valueJson.add("ownerData", ownerJson);
		JsonArray coupons = new JsonArray();
		valueJson.add("coupons", coupons);
		valueJson.addProperty("txId", stub.getTxId());
		valueJson.addProperty("timestamp", stub.getTxTimestamp().getEpochSecond());
		stub.putStringState("JDd44ca6510aea4e60a4634b6b2974f2", valueJson.toString());
		
		return newSuccessResponse();
	}

	@Override
	public Response invoke(ChaincodeStub stub) {
		_logger.info("Invoke java simple chaincode");
		String func = stub.getFunction();
		List<String> params = stub.getParameters();
		if (func.equals("invoke")) {
			return invoke(stub, params);
		}
		if (func.equals("insert")) {
			return insert(stub, params);
		}
		if (func.equals("delete")) {
			return delete(stub, params);
		}
		if (func.equals("query")) {
			return query(stub, params);
		}
		if (func.equals("queryHistory")) {
			return queryHistory(stub, params);
		}
		if (func.equals("getAll")) {
			return getAll(stub);
		}
		if (func.equals("exchangeCoupon")){
			return exchangeCoupon(stub, params);
		}

		return newErrorResponse(
				"Invalid invoke function name. Expecting one of: [\"invoke\", \"delete\", \"query\", \"insert\"]");
	}

	/** 
	 * exchange:兑换优惠券. <br/> 
	 * 
	 * @author wangchao9
	 * @param stub
	 * @param params
	 * @return 
	 * @since JDK 1.8 
	*/
	private Response exchangeCoupon(ChaincodeStub stub, List<String> params) {
		String key = params.get(0); //key
		String _amount = params.get(1);//兑换积分额度
		String _coupon = params.get(2); //优惠券编码
		long _amountLong = 0L;
		try{
			_amountLong = Long.parseLong(_amount);
		}catch(NumberFormatException e){
			return newErrorResponse(String.format("This data %s is illegal",_amount));
		}
		String value = stub.getStringState(key);
		JsonElement parse = new JsonParser().parse(value);
		if(!parse.isJsonObject()){
			return newErrorResponse(String.format("This data %s is illegal",value));
		}
		JsonObject object = parse.getAsJsonObject();
		String amount = object.get("amount").getAsString();
		long amountLong = Long.parseLong(amount);
		if(amountLong < _amountLong){
			return newErrorResponse(String.format("not enough money in account %s",amount));
		}
		JsonArray coupons = object.get("coupons").getAsJsonArray();
		coupons.add(_coupon);
		object.addProperty("txn", DESTROY_TYPE);
		object.addProperty("toKey", "destroySystem");
		object.addProperty("txId", stub.getTxId());
		object.addProperty("timestamp", stub.getTxTimestamp().getEpochSecond());
		object.addProperty("amount", amountLong - _amountLong);
		object.addProperty("remark", String.format("%s:%s",_amount,_coupon));
		object.add("coupons", coupons);
		stub.putStringState(key, object.toString());
		return newSuccessResponse("exchangeCoupon finished successfully", ByteString
				.copyFromUtf8(object.toString())
				.toByteArray());
	}

	/** 
	 * getAllKey:得到所有地址和积分余额. <br/> 
	 * 
	 * @author wangchao9
	 * @param stub
	 * @param startKey,endKey (可以为空)
	 * @return 
	 * @since JDK 1.8 
	*/
	private Response getAll(ChaincodeStub stub) {
		//JsonObject object = new JsonObject();
		QueryResultsIterator<KeyValue> keyValues = stub.getStateByRange("0", "z");
		JsonArray jsonArray = new JsonArray();
		for (KeyValue keyValue : keyValues) {
			JsonObject object = new JsonObject();
			object.addProperty("key", keyValue.getKey());
			object.addProperty("value", keyValue.getStringValue());
			jsonArray.add(object);
		}
		try {
			keyValues.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newSuccessResponse("getAll finished successfully", ByteString.copyFromUtf8(jsonArray.toString()).toByteArray());
	}

	private Response query(ChaincodeStub stub, List<String> args) {
		if (args.size() != 1) {
			return newErrorResponse("Incorrect number of arguments. Expecting name of the person to query");
		}
		String key = args.get(0);
		// byte[] stateBytes
		String value = stub.getStringState(key);
		if (value == null || value.isEmpty()) {
			return newErrorResponse(String.format("Error: state for %s is null", key));
		}
		JsonObject object = new JsonObject();
		object.addProperty("key", key);
		object.addProperty("value", value);
		_logger.info(String.format("Query Response:\nName: %s, Amount: %s\n", key, value));
		return newSuccessResponse("query finished successfully", ByteString.copyFromUtf8(object.toString()).toByteArray());
	}

	private Response delete(ChaincodeStub stub, List<String> args) {
		if (args.size() != 1) {
			return newErrorResponse("Incorrect number of arguments. Expecting 1");
		}
		String key = args.get(0);
		// Delete the key from the state in ledger
		stub.delState(key);
		return newSuccessResponse();
	}

	private Response invoke(ChaincodeStub stub, List<String> args) {
		if (args.size() != 3) {
			return newErrorResponse("Incorrect number of arguments. Expecting 3");
		}
		String accountFromKey = args.get(0);
		String accountToKey = args.get(1);

		String currentvalue = stub.getStringState(accountFromKey);
		if (currentvalue == null || currentvalue.isEmpty()) {
			return newErrorResponse(String.format("Entity %s not found", accountFromKey));
		}

		String toValue = stub.getStringState(accountToKey);
		if (toValue == null || toValue.isEmpty()) {
			return newErrorResponse(String.format("Entity %s not found", accountToKey));
		}
		
		JsonObject currentvalueJson = new JsonParser().parse(currentvalue).getAsJsonObject();
		JsonObject toValueJson = new JsonParser().parse(toValue).getAsJsonObject();
		Long accountFromValue = Long.parseLong(currentvalueJson.get("amount").getAsString());
		Long accountToValue = Long.parseLong(toValueJson.get("amount").getAsString());
		Long amount = Long.parseLong(args.get(2));
		
		if (amount > accountFromValue) {
			return newErrorResponse(String.format("not enough money in account %s", accountFromKey));
		}

		accountFromValue -= amount;
		accountToValue += amount;

		_logger.info(String.format("new value of A: %s", accountFromValue));
		_logger.info(String.format("new value of B: %s", accountToValue));
		
		currentvalueJson.addProperty("txn", TRANSACTION_TYPE);
		currentvalueJson.addProperty("amount",  Long.toString(accountFromValue));
		currentvalueJson.addProperty("toKey", accountToKey);
		currentvalueJson.addProperty("txId", stub.getTxId());
		currentvalueJson.addProperty("timestamp", stub.getTxTimestamp().getEpochSecond());
		currentvalueJson.addProperty("remark", String.format("%s",amount));
		toValueJson.addProperty("txn", TRANSACTION_TYPE);
		toValueJson.addProperty("amount",  Long.toString(accountToValue));
		toValueJson.addProperty("previousKey", accountFromKey);
		toValueJson.addProperty("txId", stub.getTxId());
		toValueJson.addProperty("timestamp", stub.getTxTimestamp().getEpochSecond());
		toValueJson.addProperty("remark", String.format("%s",amount));
		stub.putStringState(accountFromKey,currentvalueJson.toString());
		stub.putStringState(accountToKey, toValueJson.toString());
		
		_logger.info("Transfer complete");
		JsonObject object = new JsonObject();
		object.addProperty("txId", stub.getTxId());
		object.addProperty("timestamp", stub.getTxTimestamp().getEpochSecond());
		object.addProperty("currentKey", accountFromKey);
		object.addProperty("toKey", accountToKey);
		object.addProperty("amout", amount);
		object.addProperty("txn", TRANSACTION_TYPE);
		
		return newSuccessResponse("invoke finished successfully", ByteString
				.copyFromUtf8(object.toString())
				.toByteArray());
	}
	
	/**
	 * insert:初始化商户账号和积分. <br/> 
	 * 账号以UUID生成32位返回
	 * @author wangchao9
	 * @param stub
	 * @param args[ex:100] 初始化积分制,为Long
	 * @since JDK 1.8
	 */
	public Response insert(ChaincodeStub stub, List<String> args) {
		if (args.size() < 2) {
			return newErrorResponse("Less than one parameter");
		}
		String key = UUID.randomUUID().toString().replaceAll("-", "");
		String amount = args.get(0);
		String phone = args.get(1);
		String name = args.get(2);
		try{
			Long.parseLong(amount);
		}catch(NumberFormatException e){
			return newErrorResponse(String.format("For input string: %s, the value must be a numeric value.", amount));
		}
		/*初始化数据*/
		JsonObject valueJson = new JsonObject();
		valueJson.addProperty("amount", amount);
		valueJson.addProperty("txn", INIT_TYPE);
		valueJson.addProperty("previousKey", "system");
		valueJson.addProperty("currentKey", key);
		valueJson.addProperty("toKey", "");
		valueJson.addProperty("txId", stub.getTxId());
		valueJson.addProperty("timestamp", stub.getTxTimestamp().getEpochSecond());
		valueJson.addProperty("remark", String.format("init the data, key : %s ,amout : %s",key,amount));
		JsonObject ownerJson = new JsonObject();
		ownerJson.addProperty("phone", phone);
		ownerJson.addProperty("name", name);
		valueJson.add("ownerData", ownerJson);
		valueJson.add("coupons", new JsonArray());
		stub.putStringState(key, valueJson.toString());
		JsonObject object  = new JsonObject();
		object.addProperty("key", key);
		object.addProperty("value",valueJson.toString());
		return newSuccessResponse("init key finished successfully", ByteString.copyFromUtf8(object.toString()).toByteArray());
	}
	
	/**
	 * 
	 * queryHistory:根据key查询 历史数据封装成json返回. <br/> 
	 * @param stub
	 * @param args
	 * @since JDK 1.8
	 */
	private Response queryHistory(ChaincodeStub stub, List<String> args) {
		
		if (args.size() < 1) {
			return newErrorResponse("Less than one parameter");
		}
		String key = args.get(0);
		String val = stub.getStringState(key);
		if (val == null || val.isEmpty()) {
			return newErrorResponse(String.format("Entity %s not found", key));
		}
		QueryResultsIterator<KeyModification> historyForKey = stub.getHistoryForKey(key);
		JsonArray jsonArray = new JsonArray();
		for (KeyModification keyModification : historyForKey) {
			JsonObject object = new JsonObject();
			object.addProperty("key", key);
			object.addProperty("value", keyModification.getStringValue());
			object.addProperty("isDeleted", keyModification.isDeleted());
			object.addProperty("timestamp", keyModification.getTimestamp().getEpochSecond());
			object.addProperty("txId", keyModification.getTxId());
			jsonArray.add(object);
		}
		try {
			historyForKey.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newSuccessResponse("queryHistory finished successfully",
				ByteString.copyFromUtf8(jsonArray.toString()).toByteArray());
	}


    public static void main(String[] args) {
        System.out.println("OpenSSL avaliable: " + OpenSsl.isAvailable());
        new SimpleChaincode().start(args);
    }

}