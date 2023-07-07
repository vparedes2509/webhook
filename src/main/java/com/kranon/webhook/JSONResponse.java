package com.kranon.webhook;

import org.json.JSONObject;

public class JSONResponse {
	
	public static JSONObject getOAuthToken(String vsToken) {
		return new JSONObject(
				"{"
				+ "\"status\":\"Success\","
				+ "\"message\":\"Successfully generated token.\","
				+ "\"token\":\"" + vsToken + "\""
				+ "}");
	}

	public static JSONObject getErrorNoAuth() {
		return new JSONObject(
				"{"
				+ "\"status\":\"Unauthorized\","
				+ "\"message\":\"Invalid credentials.\""
				+ "}");
	}
	
	public static JSONObject getErrorToken() {
		return new JSONObject(
				"{"
				+ "\"status\":\"Unauthorized\","
				+ "\"message\":\"Invalid or expired token.\""
				+ "}");
	}
	
	public static JSONObject getErrorSecret() {
		return new JSONObject(
				"{"
				+ "\"status\":\"Unauthorized\","
				+ "\"message\":\"Invalid secret webhook.\""
				+ "}");
	}
	
	public static JSONObject getErrorBasic() {
		return new JSONObject(
				"{"
				+ "\"status\":\"Unavailable\","
				+ "\"message\":\"Basic authentication is required.\""
				+ "}");
	}
	
	public static JSONObject getErrorAuth() {
		return new JSONObject(
				"{"
				+ "\"status\":\"Unavailable\","
				+ "\"message\":\"An authentication token is required.\""
				+ "}");
	}
	
	public static JSONObject Error405() {
		return new JSONObject(
				"{" 
				+ "\"status\":\"Bad Request\"," 
				+ "\"message\":\"Method Not Allowed.\"" 
				+ "}");
	}
	
	public static JSONObject getErrorJSONInvalid() {
		return new JSONObject(
				"{" 
				+ "\"status\":\"Bad Request\"," 
				+ "\"message\":\"The request is invalid o incomplete.\"" 
				+ "}");
	}
	
	public static JSONObject getSuccessMessage() {
		return new JSONObject(
				"{" 
				+ "\"status\":\"Success\"," 
				+ "\"message\":\"Message sent/received successfully.\"" 
				+ "}");
	}
	
	public static JSONObject getErrorMessage() {
		return new JSONObject(
				"{" 
				+ "\"status\":\"Failure\"," 
				+ "\"message\":\"Error sending/receiving message.\"" 
				+ "}");
	}

}
