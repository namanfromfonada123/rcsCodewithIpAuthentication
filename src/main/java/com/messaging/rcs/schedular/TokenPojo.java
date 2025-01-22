package com.messaging.rcs.schedular;

public class TokenPojo {
	public String access_token;
	public String token_type;
	public int expires_in;
	public String scope;
	public String jti;

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public int getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(int expires_in) {
		this.expires_in = expires_in;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}

	@Override
	public String toString() {
		return "TokenPojo [access_token=" + access_token + ", token_type=" + token_type + ", expires_in=" + expires_in
				+ ", scope=" + scope + ", jti=" + jti + "]";
	}

}
