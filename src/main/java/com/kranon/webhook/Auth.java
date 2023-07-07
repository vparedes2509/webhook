package com.kranon.webhook;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.binary.Base64;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;

public class Auth { 
	private static final String USER_WEBHOOK = "Kranon";
	private static final String PASSWORD_WEBHOOK = "Kranon01#";
	private static final String SECRET_KEY = "eyJzZWNyZXQiOiJQcm9tb3RvcmEgS3Jhbm9uIFMuQS4gZGUgQy5WLiJ9";
	private static List<String> vlTokens = new ArrayList<>();

	public static boolean validateCredentials(String vsAuth) {
		String vsEncodeData = "";
		boolean vbAuthSuccess = false;
		try {
			vsEncodeData = new String(Base64.encodeBase64((USER_WEBHOOK + ":" + PASSWORD_WEBHOOK).getBytes("ISO-8859-1")));
		} catch (UnsupportedEncodingException e) {
			vsEncodeData = "";
		}
		if(vsAuth.equals(vsEncodeData)) 
			vbAuthSuccess = true;
		return vbAuthSuccess;
	}
	
	public static String generateToken(String vsAuth) {
	    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
	    byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SECRET_KEY);
	    Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	    Calendar vsFecha = Calendar.getInstance();
		vsFecha.add(Calendar.MINUTE, 15);
		String vsToken = Jwts.builder()
	            .setIssuedAt(Calendar.getInstance().getTime())
	            .setSubject("Promotora Kranon S.A. de C.V.")
	            .signWith(signatureAlgorithm, signingKey)
	            .setExpiration(vsFecha.getTime())
	            .compact();
		vlTokens.add(vsToken);
	    return vsToken;

	}
	
	public static String validateToken(String vsToken) {
		if(vsToken == null) 
			return "null";
		if(vsToken.equals("")) 
			return "";
		for(String vsTokenSave : vlTokens) {
			if(vsTokenSave.equals(vsToken)) {
				try {
					Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(SECRET_KEY)).parseClaimsJws(vsToken).getBody();
					return "Success";
				} catch(SignatureException ex) {
					System.err.println(ex.getMessage());
					return "Expire";
				}	
			}
		}
		return "Does not exist";
	}
	
	public static boolean validateTokenSHA256(String vsSignature, String vsMensaje) {
		String algorithm = "HmacSHA256";
		String vsHash = "";
		try {
			Mac sha256_hmac = Mac.getInstance(algorithm);
			SecretKeySpec secret_key = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), algorithm);
			sha256_hmac.init(secret_key);
			vsHash = Base64.encodeBase64String(sha256_hmac.doFinal(vsMensaje.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
			e.printStackTrace();
		}
		if(vsHash.equals(vsSignature)) 
			return true;
		return false;
		
	}

}
