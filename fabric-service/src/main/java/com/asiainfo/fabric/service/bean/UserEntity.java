/*
* Copyright @ 2019 com.asiainfo.com
* fabric-service 下午4:36:58
* All right reserved.
*
*/

package com.asiainfo.fabric.service.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;

import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import com.asiainfo.fabric.service.StoreManager;

import io.netty.util.internal.StringUtil;

public class UserEntity implements User, Serializable {

	private static final long serialVersionUID = -909437599629166054L;

	private String name;
	private Set<String> roles;
	private String account;
	private String affiliation;
	private String organization;
	private String enrollmentSecret;
	Enrollment enrollment = null; // need access in test env.

	private transient StoreManager keyValStore;
	private String keyValStoreName;

	public UserEntity(String name, String org, StoreManager fs) {
	        this.name = name;

	        this.keyValStore = fs;
	        this.organization = org;
	        this.keyValStoreName = toKeyValStoreName(this.name, org);
	        String memberStr = keyValStore.getValue(keyValStoreName);
	        if (null == memberStr) {
	            saveState();
	        } else {
	            restoreState();
	        }

	    }

	public static boolean isStored(String name, String org, StoreManager fs) {

		return fs.hasValue(toKeyValStoreName(name, org));
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Set<String> getRoles() {
		return this.roles;
	}

	public void setRoles(Set<String> roles) {

		this.roles = roles;
		saveState();
	}

	@Override
	public String getAccount() {
		return this.account;
	}

	/**
	 * Set the account.
	 *
	 * @param account
	 *            The account.
	 */
	public void setAccount(String account) {

		this.account = account;
		saveState();
	}

	@Override
	public String getAffiliation() {
		return this.affiliation;
	}

	/**
	 * Set the affiliation.
	 *
	 * @param affiliation
	 *            the affiliation.
	 */
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
		saveState();
	}

	@Override
	public Enrollment getEnrollment() {
		return this.enrollment;
	}

	/**
	 * Determine if this name has been registered.
	 *
	 * @return {@code true} if registered; otherwise {@code false}.
	 */
	public boolean isRegistered() {
		return !StringUtil.isNullOrEmpty(enrollmentSecret);
	}

	/**
	 * Determine if this name has been enrolled.
	 *
	 * @return {@code true} if enrolled; otherwise {@code false}.
	 */
	public boolean isEnrolled() {
		return this.enrollment != null;
	}

	/**
	 * Save the state of this user to the key value store.
	 */
	public void saveState() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			oos.flush();
			keyValStore.setValue(keyValStoreName, Hex.toHexString(bos.toByteArray()));
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Restore the state of this user from the key value store (if found). If
	 * not found, do nothing.
	 */
	UserEntity restoreState() {
		String memberStr = keyValStore.getValue(keyValStoreName);
		if (null != memberStr) {
			// The user was found in the key value store, so restore the
			// state.
			byte[] serialized = Hex.decode(memberStr);
			ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
			try {
				ObjectInputStream ois = new ObjectInputStream(bis);
				UserEntity state = (UserEntity) ois.readObject();
				if (state != null) {
					this.name = state.name;
					this.roles = state.roles;
					this.account = state.account;
					this.affiliation = state.affiliation;
					this.organization = state.organization;
					this.enrollmentSecret = state.enrollmentSecret;
					this.enrollment = state.enrollment;
					this.mspId = state.mspId;
					return this;
				}
			} catch (Exception e) {
				throw new RuntimeException(String.format("Could not restore state of member %s", this.name), e);
			}
		}
		return null;
	}

	public String getEnrollmentSecret() {
		return enrollmentSecret;
	}

	public void setEnrollmentSecret(String enrollmentSecret) {
		this.enrollmentSecret = enrollmentSecret;
		saveState();
	}

	public void setEnrollment(Enrollment enrollment) {

		this.enrollment = enrollment;
		saveState();

	}

	public static String toKeyValStoreName(String name, String org) {
		return "user." + name + org;
	}

	@Override
	public String getMspId() {
		return mspId;
	}

	String mspId;

	public void setMspId(String mspID) {
		this.mspId = mspID;
		saveState();

	}
}
