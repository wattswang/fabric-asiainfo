/** 
* Project Name:fabric-service 
* File Name:ChaincodeRequest.java 
* Package Name:com.asiainfo.fabric.service 
* Date:2019年1月21日下午5:19:43 
* Copyright (c) 2019, wangchao9@asiainfo.com All Rights Reserved. 
**/
package com.asiainfo.fabric.service;

import java.util.ArrayList;
import java.util.Arrays;

/** 
* ClassName:ChaincodeRequest <br/> 
* Function: TODO ADD FUNCTION. <br/> 
* Reason: TODO ADD REASON. <br/> 
* Date: 2019年1月21日 下午5:19:43 <br/> 
* @author wangchao9 
* @version 
* @since JDK 1.8 
* @see 
*/
public class ChaincodeRequest {
	
	private String fuc;
	private ArrayList<String> args;
	/**
	 * @return the fuc
	 */
	public String getFuc() {
		return fuc;
	}
	/**
	 * @param fuc the fuc to set
	 */
	public void setFuc(String fuc) {
		this.fuc = fuc;
	}
	/**
	 * @return the args
	 */
	public ArrayList<String> getArgs() {
		return args;
	}
	/**
	 * @param args the args to set
	 */
	public void setArgs(String... args) {
		this.args = new ArrayList<>(Arrays.asList(args));
	}
	
	class ChaincodeExecOp{
		public static final String QUERY = "query"; //根据账户地址查询账户目前信息,参数为key : JDd44ca6510aea4e60a4634b6b2974f2
		public static final String GETALL = "getAll";//无需参数
		public static final String QUERYHISTORY = "queryHistory";//根据账户信息查询当前账户历史信息：参数为key example: JDd44ca6510aea4e60a4634b6b2974f2
		public static final String INSERT = "insert";//初始化数据：参数为积分余额,电话号码,姓名 example: "100","15189839843","陈三"
		public static final String INVOKE = "invoke";//积分交易：参数为源账户地址,目标账户地址,积分数量 example: "JDd44ca6510aea4e60a4634b6b2974f2","4974d64083e14b2bb45cc20f283c0fe7","100"
		public static final String EXCHANGECOUPON = "exchangeCoupon";//积分兑换优惠券：参数为兑换账户,兑换积分,兑换优惠券编码。example:"JDd44ca6510aea4e60a4634b6b2974f2","100","EDSFSSSSSFKL"
	}
	
	
}

