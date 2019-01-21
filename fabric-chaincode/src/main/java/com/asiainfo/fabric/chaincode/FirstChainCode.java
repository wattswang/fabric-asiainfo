package com.asiainfo.fabric.chaincode;

import java.time.Instant;
import java.util.Arrays;

import org.hyperledger.fabric.protos.peer.ChaincodeEventPackage.ChaincodeEvent;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FirstChainCode extends ChaincodeBase{

	@Override
	public Response init(ChaincodeStub arg0) {
		return null;
	}

	@Override
	public Response invoke(ChaincodeStub stub) {
		ChaincodeEvent event = stub.getEvent();
		stub.getTxId();
		Instant txTimestamp = stub.getTxTimestamp();
		long epochSecond = txTimestamp.getEpochSecond();
		stub.putStringState("", "");
		QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult("");
		QueryResultsIterator<KeyModification> historyForKey = stub.getHistoryForKey("");
		for (KeyModification keyModification : historyForKey) {
			String txId = keyModification.getTxId();
		}
		for (KeyValue keyValue : queryResult) {
			keyValue.getKey();
		}
		
		JsonObject object = new JsonObject();
		object.addProperty("txId", "123131");
		object.addProperty("timestamp", "timestamp");
		return null;
	}
	
	public static void main(String[] args) {
		String key = "JDd44ca6510aea4e60a4634b6b2974f2";
		String amount = "10000000000";
		/*初始化数据*/
		JsonObject valueJson = new JsonObject();
		valueJson.addProperty("amount", amount);
		valueJson.addProperty("txn", "INIT");
		valueJson.addProperty("phone", "10000086");
		valueJson.addProperty("fromAddr", key);
		valueJson.addProperty("toAddr", "");
		valueJson.addProperty("remark", String.format("init the data, key : %S ,amout ： %s",key,amount));
		//stub.putStringState("JDd44ca6510aea4e60a4634b6b2974f2", "10000000000");
		JsonObject ownerJson = new JsonObject();
		ownerJson.addProperty("phone", "10000086");
		ownerJson.addProperty("name", "京东");
		valueJson.add("ownerData", ownerJson);
		JsonArray coupons = new JsonArray();
		valueJson.add("coupons", coupons);
        //System.out.println(valueJson.toString());
        
        
        JsonParser pareser = new JsonParser();
        String str = "{\"key\":\"JDd44ca6510aea4e60a4634b6b2974f2\",\"value\":\"{\\\"amount\\\":\\\"10000000000\\\",\\\"txn\\\":\\\"INIT\\\",\\\"currentKey\\\":\\\"JDd44ca6510aea4e60a4634b6b2974f2\\\",\\\"previousKey\\\":\\\"system\\\",\\\"toKey\\\":\\\"\\\",\\\"remark\\\":\\\"init the data, key : JDD44CA6510AEA4E60A4634B6B2974F2 ,amout is 10000000000\\\",\\\"ownerData\\\":{\\\"phone\\\":\\\"10000086\\\",\\\"name\\\":\\\"JD\\\"},\\\"coupons\\\":[],\\\"txId\\\":\\\"0edadc135b048673f2dae5cf54be200edb7f6b93903f41d26e98b5829f2f6485\\\",\\\"timestamp\\\":1548042113}\"}";
        JsonElement parse =  pareser.parse(str);
        System.out.println(parse.isJsonObject());
        JsonObject asJsonObject = parse.getAsJsonObject();
        JsonElement jsonElement = asJsonObject.get("value");
        String jsonStr = "{\"amount\":\"10000000000\",\"txn\":\"INIT\",\"currentKey\":\"JDd44ca6510aea4e60a4634b6b2974f2\",\"previousKey\":\"system\",\"toKey\":\"\",\"remark\":\"init the data, key : JDD44CA6510AEA4E60A4634B6B2974F2 ,amout is 10000000000\",\"ownerData\":{\"phone\":\"10000086\",\"name\":\"JD\"},\"coupons\":[],\"txId\":\"0edadc135b048673f2dae5cf54be200edb7f6b93903f41d26e98b5829f2f6485\",\"timestamp\":1548042113}";
        //String asString = jsonElement.getAsJsonObject().get("phone").getAsString();
        JSONObject parse2 = (JSONObject) JSON.parse(jsonStr);
        System.out.println(parse2.toString());
        
        String valueStr = valueJson.toString();
        JsonParser pareser1 = new JsonParser();
        JsonObject asJsonObject2 = pareser1.parse(valueStr).getAsJsonObject();
        JsonArray _coupons = asJsonObject2.get("counpons").getAsJsonArray();
        _coupons.add("EDSFSSSSFF");
        valueJson.add("counpons", _coupons);
        System.out.println(valueJson.toString());
        
        String[] strs = new String[]{"0","a","JDd44ca6510aea4e60a4634b6b2974f2","z","9999","a974d64083e14b2bb45cc20f283c0fe7","Z"};
        Arrays.sort(strs);
        for (String string : strs) {
			System.out.println(string);
		}
	}

}
