/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.cert.toolkit.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;

/**
 * @author yuzhichu
 * @author wesleywang
 */
@Slf4j
public class KeyUtils {


	public static KeyPair generateKeyPair(){
		KeyPairGenerator keyPairGen = null;
		KeyPair keyPair = null;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA");
			keyPairGen.initialize(2048);
			keyPair =  keyPairGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			log.error("generateKeyPair failed", e);
		}
		return keyPair;
	}

	public static PublicKey getRSAPublicKey(String key) throws Exception {
		byte[] keyBytes = Base64.decodeBase64(key);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(keySpec);
	}

	public static PrivateKey getRSAPrivateKey(String key) throws Exception {
		key = key.replace("-----BEGIN RSA PRIVATE KEY-----", "");
		key = key.replace("-----END RSA PRIVATE KEY-----", "");
		byte[] keyBytes = Base64.decodeBase64(key);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	public static PublicKey getRSAPublicKey(PrivateKey privateKey) throws Exception {
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPrivateKeySpec spec = keyFactory.getKeySpec(privateKey,RSAPrivateKeySpec.class);
		RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(spec.getModulus(),
				BigInteger.valueOf(65537));
		return keyFactory.generatePublic(publicKeySpec);
	}

	public static KeyPair getECKeyPair(String privateStr) throws Exception {
		PEMKeyPair pemObject = (PEMKeyPair) CertUtils.readStringAsPEM(privateStr);
		PrivateKey privateKey = KeyFactory.getInstance("EC").generatePrivate(
				new PKCS8EncodedKeySpec(pemObject.getPrivateKeyInfo().getEncoded()));
		PublicKey publicKey = getPublicKey((ECPrivateKey) privateKey);
		return new KeyPair(publicKey,privateKey);
	}

	public static KeyPair getRSAKeyPair(String privateStr) throws Exception {
		PEMKeyPair pemObject = (PEMKeyPair) CertUtils.readStringAsPEM(privateStr);
		if (pemObject == null) {
			throw new RuntimeException("missing pemPrivateKey string coding");
		}
		PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
				new PKCS8EncodedKeySpec(pemObject.getPrivateKeyInfo().getEncoded()));
		PublicKey publicKey = getRSAPublicKey(privateKey);
		return new KeyPair(publicKey,privateKey);
	}


	public static PublicKey getPublicKey(ECPrivateKey privateKey)
			throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
		ECParameterSpec params = privateKey.getParams();
		org.bouncycastle.jce.spec.ECParameterSpec bcSpec =
				org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util.convertSpec(params, false);
		org.bouncycastle.math.ec.ECPoint q = bcSpec.getG().multiply(privateKey.getS());
		org.bouncycastle.math.ec.ECPoint bcW = bcSpec.getCurve().decodePoint(q.getEncoded(false));
		ECPoint w =
				new ECPoint(
						bcW.getAffineXCoord().toBigInteger(), bcW.getAffineYCoord().toBigInteger());
		ECPublicKeySpec keySpec = new ECPublicKeySpec(w, tryFindNamedCurveSpec(params));
		return KeyFactory.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME)
				.generatePublic(keySpec);
	}

	@SuppressWarnings("unchecked")
	private static ECParameterSpec tryFindNamedCurveSpec(ECParameterSpec params) {
		org.bouncycastle.jce.spec.ECParameterSpec bcSpec =
				org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util.convertSpec(params, false);
		for (Object name : Collections.list(org.bouncycastle.jce.ECNamedCurveTable.getNames())) {
			org.bouncycastle.jce.spec.ECNamedCurveParameterSpec bcNamedSpec =
					org.bouncycastle.jce.ECNamedCurveTable.getParameterSpec((String) name);
			if (bcNamedSpec.getN().equals(bcSpec.getN())
					&& bcNamedSpec.getH().equals(bcSpec.getH())
					&& bcNamedSpec.getCurve().equals(bcSpec.getCurve())
					&& bcNamedSpec.getG().equals(bcSpec.getG())) {
				return new org.bouncycastle.jce.spec.ECNamedCurveSpec(
						bcNamedSpec.getName(),
						bcNamedSpec.getCurve(),
						bcNamedSpec.getG(),
						bcNamedSpec.getN(),
						bcNamedSpec.getH(),
						bcNamedSpec.getSeed());
			}
		}
		return params;
	}
}
















