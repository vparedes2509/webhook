package com.kranon.webhook;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;
import com.kranon.webhook.purecloud.MetodosPureCloud;
import com.kranon.webhook.utilidades.Utilerias;

public class OpenMessaging {
	static {
        System.setProperty("dateLog", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
    }
	private static final Logger voLogger = LogManager.getLogger("OpenMessaging");
	private Utilerias voUtil = null;

	private String voPathConf = "D:/Appl/KRANON/OpenMessaging/Configuraciones/conf.properties";
	private Map<String, String> voMapConf = null;
	
	public OpenMessaging() {
		MetodosPureCloud voPurecloud = new MetodosPureCloud();
		voUtil = new Utilerias();
		voMapConf = new HashMap<>();
		
		if (voUtil.getProperties(voPathConf, voMapConf)) {
			voPurecloud.setCredentials(voMapConf.get("ClientID"), voMapConf.get("ClientSecret"));
			voPurecloud.setDeployment(voMapConf.get("Deployment"));
			
			voLogger.info("********************************************************************");
			voLogger.info("METODO SPARK INICIADO, PUERTO:[" + voMapConf.get("Puerto") + "]");
			Spark.port(Integer.valueOf(voMapConf.get("Puerto")));
			HashMap<String, String> corsHeaders = new HashMap<>();
			corsHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
			corsHeaders.put("Access-Control-Allow-Origin", "*");
			corsHeaders.put("Access-Control-Allow-Headers","Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
			corsHeaders.put("Access-Control-Allow-Credentials", "true");
			Filter filter = new Filter() {
				@Override
				public void handle(Request request, Response response) throws Exception {
					corsHeaders.forEach((key, value) -> {
						response.header(key, value);
					});
				}
			};
			Spark.after(filter);

			//***********************Auth token*************************
			Spark.post("/oauth/token", (request, response) -> {
				response.type("application/json");
				response.status(405);
				voLogger.error("ERROR : Method [POST] not allowed");
				return JSONResponse.Error405();
			});
			Spark.put("/oauth/token", (request, response) -> {
				response.type("application/json");
				response.status(405);
				voLogger.error("ERROR : Method [PUT] not allowed");
				return JSONResponse.Error405();
			});
			Spark.delete("/oauth/token", (request, response) -> {
				response.type("application/json");
				response.status(405);
				voLogger.error("ERROR : Method [DELETE] not allowed");
				return JSONResponse.Error405();
			});
			Spark.options("/oauth/token", (request, response) -> {
				response.type("application/json");
				response.status(405);
				voLogger.error("ERROR : Method [OPTIONS] not allowed");
				return JSONResponse.Error405();
			});
			Spark.patch("/oauth/token", (request, response) -> {
				response.type("application/json");
				response.status(405);
				voLogger.error("ERROR : Method [PATCH] not allowed");
				return JSONResponse.Error405();
			});
			
			Spark.get("/oauth/token", (request, response) -> {
				String vsUUI = java.util.UUID.randomUUID().toString();
				response.type("application/json");
				String vsAuth = request.headers("Authorization");
				if(vsAuth == null) {
					voLogger.error("[" + vsUUI + "] --> The authorization is null.");
					response.status(503);
					return JSONResponse.getErrorBasic();
				}
				if(vsAuth.contains("Basic")) {
					vsAuth = vsAuth.replace("Basic ", "");
					if(Auth.validateCredentials(vsAuth)) {
						voLogger.info("[" + vsUUI + "] --> Authorization basic successsful.");
						response.status(200);
						return JSONResponse.getOAuthToken(Auth.generateToken(vsAuth));
					} else {
						voLogger.error("[" + vsUUI + "] --> Does not contain authorization credentials.");
						response.status(401);
						return JSONResponse.getErrorNoAuth();
					}
				} else {
					voLogger.error("[" + vsUUI + "] --> Authorization is not basic.");
					response.status(503);
					return JSONResponse.getErrorBasic();
				}		
			});
			//**********************************************************

			
			//*********************MessageToGenesys***********************
			Spark.get("/messageToGenesys", (request, response) -> {
				response.type("application/json");
				response.status(405);
				voLogger.error("ERROR : Method [GET] not allowed");
				return JSONResponse.Error405();
			});
			Spark.put("/messageToGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [PUT] not allowed");
				return JSONResponse.Error405();
			});
			Spark.delete("/messageToGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [DELETE] not allowed");
				return JSONResponse.Error405();
			});
			Spark.options("/messageToGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [OPTIONS] not allowed");
				return JSONResponse.Error405();
			});
			Spark.patch("/messageToGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [PATCH] not allowed");
				return JSONResponse.Error405();
			});
			
			Spark.post("/messageToGenesys", (request, response) -> {
				response.type("application/json");
				String vsUUI = java.util.UUID.randomUUID().toString();
				response.status(400);
				String vsAuth = request.headers("Authorization");
				
				if(vsAuth == null) {
					voLogger.error("[" + vsUUI + "] --> The authorization is null.");
					response.status(503);
					return JSONResponse.getErrorAuth();
				}
				
				if(!vsAuth.contains("Bearer")) {
					voLogger.error("[" + vsUUI + "] --> Authorization is invalid.");
					response.status(503);
					return JSONResponse.getErrorAuth();
				}
				
				String vsEstatus = Auth.validateToken(vsAuth.replace("Bearer ", ""));
				if(vsEstatus.equals("Success")) {
					
					voLogger.info("[" + vsUUI + "] --> Sending message to genesys.");
					if(request.body().equals("")) {
						voLogger.error("[" + vsUUI + "] --> The request is empty.");
						return JSONResponse.getErrorJSONInvalid();
					}
					JSONObject voJSONRequest = new JSONObject(request.body());
			    	if(!voJSONRequest.has("message") || !voJSONRequest.has("from")) {
			    		voLogger.error("[" + vsUUI + "] --> The request is imcomplete.");
			    		return JSONResponse.getErrorJSONInvalid();
			    	}
					String vsFechaActual = new DateTime().withZone(DateTimeZone.UTC).toString();
					System.out.println("MENSAJE ENVIADO POR INSOMNIA : " + voJSONRequest.getString("message"));
					if(voPurecloud.sendMessageToGenesys(false, voJSONRequest.getString("from"), vsFechaActual, voJSONRequest.getString("message"))) {
						voLogger.info("[" + vsUUI + "] --> Message sent succesfully.");
						response.status(200);
						return JSONResponse.getSuccessMessage();
					} else {
						voLogger.error("[" + vsUUI + "] --> Error sending message.");
						return JSONResponse.getErrorMessage();
					}
					
				}else {
					voLogger.error("[" + vsUUI + "] --> The token is invalid o expired, token=[" + vsEstatus + "].");
					response.status(401);
					return JSONResponse.getErrorToken();
				} 
			});
			//**********************************************************

			
			//********************* MessageFromGenesys ***********************
			Spark.get("/messageFromGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [GET] not allowed");
				return JSONResponse.Error405();
			});
			Spark.put("/messageFromGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [PUT] not allowed");
				return JSONResponse.Error405();
			});
			Spark.delete("/messageFromGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [DELETE] not allowed");
				return JSONResponse.Error405();
			});
			Spark.options("/messageFromGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [OPTIONS] not allowed");
				return JSONResponse.Error405();
			});
			Spark.patch("/messageFromGenesys", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [PATCH] not allowed");
				return JSONResponse.Error405();
			});
			Spark.post("/messageFromGenesys", (request, response) -> {
				String vsSignature = request.headers("X-Hub-Signature-256");
				response.type("application/json");
				if(Auth.validateTokenSHA256(vsSignature.replace("sha256=", ""), request.body())) {
					response.status(200);
					System.out.println("MENSAJE ENVIADO POR EL AGENTE : " + new JSONObject(request.body()).getString("text"));
					return JSONResponse.getSuccessMessage();
				}
				response.status(401);
				return JSONResponse.getErrorSecret();
			});
			
			//*****************************************************************
			
			//********************* messageAgentless ***********************
			Spark.get("/messageAgentless", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [GET] not allowed");
				return JSONResponse.Error405();
			});
			Spark.put("/messageAgentless", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [PUT] not allowed");
				return JSONResponse.Error405();
			});
			Spark.delete("/messageAgentless", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [DELETE] not allowed");
				return JSONResponse.Error405();
			});
			Spark.options("/messageAgentless", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [OPTIONS] not allowed");
				return JSONResponse.Error405();
			});
			Spark.patch("/messageAgentless", (request, response) -> {
				response.status(405);
				response.type("application/json");
				voLogger.error("ERROR : Method [PATCH] not allowed");
				return JSONResponse.Error405();
			});
			
			Spark.post("/messageAgentless", (request, response) -> {
				String vsUUI = java.util.UUID.randomUUID().toString();
				response.type("application/json");
				response.status(400);
				String vsAuth = request.headers("Authorization");
				
				if(vsAuth == null) {
					voLogger.error("[" + vsUUI + "] --> The authorization is null.");
					response.status(503);
					return JSONResponse.getErrorAuth();
				}
				
				if(!vsAuth.contains("Bearer")) {
					voLogger.error("[" + vsUUI + "] --> Authorization is invalid.");
					response.status(503);
					return JSONResponse.getErrorAuth();
				}
				
				String vsEstatus = Auth.validateToken(vsAuth.replace("Bearer ", ""));
				if(vsEstatus.equals("Success")) {
					
					voLogger.info("[" + vsUUI + "] --> Sending message to genesys.");
					if(request.body().equals("")) {
						voLogger.error("[" + vsUUI + "] --> The request is empty.");
						return JSONResponse.getErrorJSONInvalid();
					}
					JSONObject voJSONRequest = new JSONObject(request.body());
			    	if(!voJSONRequest.has("message") || !voJSONRequest.has("to")) {
			    		voLogger.error("[" + vsUUI + "] --> The request is imcomplete.");
			    		return JSONResponse.getErrorJSONInvalid();
			    	}
					System.out.println("MENSAJE ENVIADO SIN AGENTE : " + voJSONRequest.getString("message"));
					boolean vbStatus = voPurecloud.sendMessageFromGenesysAgentless(false, voJSONRequest.getString("to"), voJSONRequest.getString("message"));
					if(vbStatus) {
						voLogger.info("[" + vsUUI + "] --> Message sent succesfully.");
						response.status(200);
						return JSONResponse.getSuccessMessage();
					} else {
						voLogger.error("[" + vsUUI + "] --> Error sending message.");
						return JSONResponse.getErrorMessage();
					}
					
				}else {
					voLogger.error("[" + vsUUI + "] --> The token is invalid o expired, status token=[" + vsEstatus + "].");
					response.status(401);
					return JSONResponse.getErrorToken();
				} 
			});
			
			//****************************************************************************************+
		}
		
	}
	
	public static void main(String [] args) {
		new OpenMessaging();
	}

}
