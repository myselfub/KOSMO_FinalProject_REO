package kr.co.reo.client.member;

public class AuthInfo {
	private String clientId;
	private String clientSecret;

	public AuthInfo(String clientId, String clientSecret) {
		this.clientId = "****";
		this.clientSecret = "****";
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}
}
