package com.kranon.webhook.purecloud;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import com.kranon.webhook.conexionHttp.ConexionHttp;
import com.kranon.webhook.conexionHttp.ConexionResponse;

public class MetodosPureCloud {
	private Integer viTimeOut = 15000;
	public String vsToken = "qvdMSdL4OdUU8m2qvIDatjSXY6YvIR5C9ezNLJTy5p0WVWDYUE0vvrYunRz-1j8agDRmtkNfF27NmQFcy6SIvA";
	
	private String vsDeploymentID = "4b0498bf-3793-4826-9642-85a6f1749284";
	public void setDeployment(String vsDeploymentID) {
		this.vsDeploymentID = vsDeploymentID;
	}
	
	private String vsClientID = "";
	private String vsClientSecret = "";
	public void setCredentials(String vsClientID, String vsClientSecret) {
		this.vsClientID = vsClientID;
		this.vsClientSecret = vsClientSecret;
	}

	private boolean getToken(String vsClientID, String vsClientSec) {
		String URLServicio = "https://login.mypurecloud.com/oauth/token?grant_type=client_credentials";
		HashMap<String, String> header = new HashMap<>();
		try {
			String vsEncodeData = new String(Base64.encodeBase64((vsClientID + ":" + vsClientSec).getBytes("ISO-8859-1")));
			header.put("Authorization", "Basic " + vsEncodeData);
			ConexionHttp conexionHttp = new ConexionHttp();
			ConexionResponse conexionResponse = conexionHttp.executePost(URLServicio, viTimeOut, "", header);
			if (conexionResponse.getCodigoRespuesta() == 200) {
				JSONObject json = new JSONObject(conexionResponse.getMensajeRespuesta());
				if (json.has("access_token")) {
					vsToken = json.getString("access_token");
					System.out.println("TOKEN PC: " + vsToken);
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	public boolean sendMessageFromGenesysAgentless(boolean vbRecursivo, String vsToAddress, String vsBody) {
		String vsURLServicio = "https://api.mypurecloud.com/api/v2/conversations/messages/agentless";
		HashMap<String, String> voHeader = new HashMap<String, String>();
		voHeader.put("Authorization", "bearer " + vsToken);
		String vsBodyMensaje = "{\r\n" 
				+ "  \"fromAddress\": \"" + vsDeploymentID + "\",\r\n"
				+ "  \"toAddress\": \"" +  vsToAddress + "\",\r\n" 
				+ "  \"toAddressMessengerType\": \"open\",\r\n"
				+ "  \"textBody\": \"" + vsBody + "\"\r\n" 
				+ "}";

		ConexionHttp conexionHttp = new ConexionHttp();
		ConexionResponse conexionResponse = null;
		try {
			conexionResponse = conexionHttp.executePost(vsURLServicio, viTimeOut, vsBodyMensaje, voHeader);
			if(conexionResponse.getCodigoRespuesta() == 202) return true;
			 else {
				if(!vbRecursivo && conexionResponse.getCodigoRespuesta() == 401) {
					getToken(vsClientID, vsClientSecret);
					return sendMessageFromGenesysAgentless(true, vsToAddress, vsBody);
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	

	public boolean sendMessageToGenesys(boolean vbRecursivo, String vsFromID, String vsTime, String vsMessageText) {
		String vsConversationID = "";
		try {
			vsConversationID = new String(Base64.encodeBase64((vsDeploymentID + ":" + vsFromID).getBytes("ISO-8859-1")));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			vsConversationID = java.util.UUID.randomUUID().toString();
		}
		String vsMessageID = java.util.UUID.randomUUID().toString();	
		String vsURLServicio = "https://api.mypurecloud.com/api/v2/conversations/messages/inbound/open";
		String vsBodyMensaje = 
				"{\r\n"
				+ "  \"id\": \"" + vsConversationID + "\",\r\n"
				+ "  \"channel\": {\r\n"
				+ "    \"platform\": \"Open\",\r\n"
				+ "    \"type\": \"Private\",\r\n"
				+ "		\"messageId\":\"" + vsMessageID + "\",\r\n"
				+ "    \"to\": {\r\n"
				+ "      \"id\": \"" + vsDeploymentID + "\"\r\n"
				+ "    },\r\n"
				+ "    \"from\": {\r\n"
				+ "      \"id\": \"" + vsFromID + "\"\r\n"
				+ "    },\r\n"
				+ "    \"time\": \"" + vsTime + "\"\r\n"
				+ "  },\r\n"
				+ "  \"type\": \"Text\",\r\n"
				+ "  \"text\": \"" + vsMessageText + "\",\r\n"
				+ "  \"direction\": \"Inbound\"\r\n"
				+ "}";

		HashMap<String, String> voHeader = new HashMap<String, String>();
		voHeader.put("Authorization", "bearer " + vsToken);
		try {
			ConexionHttp conexionHttp = new ConexionHttp();
			ConexionResponse conexionResponse = conexionHttp.executePost(vsURLServicio, viTimeOut, vsBodyMensaje, voHeader);
			
			if(conexionResponse.getCodigoRespuesta() == 202) return true;
			 else {
				if(!vbRecursivo && conexionResponse.getCodigoRespuesta() == 401) {
					getToken(vsClientID, vsClientSecret);
					return sendMessageToGenesys(true, vsFromID, vsTime, vsMessageText);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
