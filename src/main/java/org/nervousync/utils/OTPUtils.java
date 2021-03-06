/*
 * Licensed to the Nervousync Studio (NSYC) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nervousync.utils;

import org.nervousync.commons.core.Globals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * TOTP(Time-based One-time Password Algorithm) Utility
 *
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision : 1.0 $ $Date: 2019-06-04 10:47 $
 */
public final class OTPUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(OTPUtils.class);

	//  Unit: Second
	private static final int DEFAULT_SYNC_COUNT = 30;

	//  Default 3, Maximum 17 (From Google Docs)
	private static final int DEFAULT_WINDOW_SIZE = 3;
	private static final int DEFAULT_SECRET_SIZE = 10;
	private static final String DEFAULT_SECRET_SEED = "TmVydm91c3luY0RlZmF1bHRTZWNyZXRTZWVk";
	private static final String DEFAULT_RANDOM_ALGORITHM = "SHA1PRNG";

	/**
	 * Calculate fixed time
	 *
	 * @param randomKey Random key
	 * @param authCode  Auth code
	 * @return Fixed time
	 */
	public static long calculateFixedTime(String randomKey, int authCode) {
		return calculateFixedTime(CalcType.HmacSHA1, randomKey, authCode, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * Calculate fixed time
	 *
	 * @param randomKey Random key
	 * @param authCode  Auth code
	 * @param syncCount Synchronize count
	 * @return Fixed time
	 */
	public static long calculateFixedTime(String randomKey, int authCode, int syncCount) {
		return calculateFixedTime(CalcType.HmacSHA1, randomKey, authCode, syncCount);
	}

	/**
	 * Calculate fixed time
	 *
	 * @param calcType  Calculate type
	 * @param randomKey Random key
	 * @param authCode  Auth code
	 * @return Fixed time
	 */
	public static long calculateFixedTime(CalcType calcType, String randomKey, int authCode) {
		return calculateFixedTime(calcType, randomKey, authCode, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * Calculate fixed time
	 *
	 * @param calcType  Calculate type
	 * @param randomKey Random key
	 * @param authCode  Auth code
	 * @param syncCount Synchronize count
	 * @return Fixed time
	 */
	public static long calculateFixedTime(CalcType calcType, String randomKey, int authCode, int syncCount) {
		for (int i = -12 ; i <= 12 ; i++) {
			long fixedTime = i * 60 * 60 * 1000L;
			if (validateTOTPCode(calcType, authCode, randomKey, fixedTime, syncCount, Globals.INITIALIZE_INT_VALUE)) {
				return fixedTime;
			}
		}
		return Globals.DEFAULT_VALUE_INT;
	}

	/**
	 * Generate auth code
	 *
	 * @param secret    Secret string
	 * @param fixedTime Fixed time
	 * @return Auth code
	 */
	public static String generateTOTPCode(String secret, long fixedTime) {
		return generateTOTPCode(CalcType.HmacSHA1, secret, fixedTime, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * Generate auth code
	 *
	 * @param secret    Secret string
	 * @param fixedTime Fixed time
	 * @param syncCount Synchronize count
	 * @return Auth code
	 */
	public static String generateTOTPCode(String secret, long fixedTime, int syncCount) {
		return generateTOTPCode(CalcType.HmacSHA1, secret, fixedTime, syncCount);
	}

	/**
	 * Generate auth code
	 *
	 * @param calcType  Calculate type
	 * @param secret    Secret string
	 * @param fixedTime Fixed time
	 * @return Auth code
	 */
	public static String generateTOTPCode(CalcType calcType, String secret, long fixedTime) {
		return generateTOTPCode(calcType, secret, fixedTime, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * Generate auth code
	 *
	 * @param calcType  Calculate type
	 * @param secret    Secret string
	 * @param fixedTime Fixed time
	 * @param syncCount Synchronize count
	 * @return Auth code
	 */
	public static String generateTOTPCode(CalcType calcType, String secret, long fixedTime, int syncCount) {
		int authCode = OTPUtils.generateTOTPCode(calcType, secret,
				fixedTime, syncCount, Globals.INITIALIZE_INT_VALUE);
		if (authCode == Globals.DEFAULT_VALUE_INT) {
			return Globals.DEFAULT_VALUE_STRING;
		}

		StringBuilder returnCode = new StringBuilder(Integer.toString(authCode));
		while (returnCode.length() < 6) {
			returnCode.insert(0, "0");
		}
		return returnCode.toString();
	}

	/**
	 * Generate random secret key using default algorithm, seed and seed size
	 *
	 * @return Random secret key
	 */
	public static String generateRandomKey() {
		return generateRandomKey(DEFAULT_RANDOM_ALGORITHM, DEFAULT_SECRET_SEED, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * Generate random secret key using default algorithm and seed
	 *
	 * @param size seed size
	 * @return Random secret key
	 */
	public static String generateRandomKey(int size) {
		return generateRandomKey(DEFAULT_RANDOM_ALGORITHM, DEFAULT_SECRET_SEED, size);
	}

	/**
	 * Generate random secret key by given algorithm, seed and seed size
	 *
	 * @param algorithm Secure algorithm
	 * @param seed      Secret seed
	 * @param size      Seed size
	 * @return Random secret key
	 */
	public static String generateRandomKey(String algorithm, String seed, int size) {
		String randomKey = null;
		try {
			SecureRandom secureRandom = StringUtils.notBlank(algorithm)
					? SecureRandom.getInstance(algorithm)
					: new SecureRandom();
			if (StringUtils.notBlank(seed)) {
				secureRandom.setSeed(StringUtils.base64Decode(seed));
			}
			byte[] randomKeyBytes =
					secureRandom.generateSeed(size == Globals.DEFAULT_VALUE_INT ? DEFAULT_SECRET_SIZE : size);
			randomKey = StringUtils.base32Encode(randomKeyBytes, Globals.DEFAULT_VALUE_BOOLEAN);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Generate random key error!");
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Stack message: ", e);
			}
		}
		return randomKey;
	}

	/**
	 * Generate HOTP auth code using default calculate type: HmacSHA1
	 *
	 * @param randomKey  random secret key
	 * @param randomCode random code
	 * @return generated code
	 */
	public static int generateHOTPCode(String randomKey, long randomCode) {
		return generateCode(CalcType.HmacSHA1, randomKey, randomCode);
	}

	/**
	 * Generate HOTP auth code
	 *
	 * @param calcType   Calculate type
	 * @param randomKey  random secret key
	 * @param randomCode random code
	 * @return generated code
	 */
	public static int generateHOTPCode(CalcType calcType, String randomKey, long randomCode) {
		return generateCode(calcType, randomKey, randomCode);
	}

	/**
	 * Validate auth code by given secret key and fixed time using default calculate type: HmacSHA1
	 *
	 * @param authCode  auth code
	 * @param randomKey random secret key
	 * @param fixedTime fixed time
	 * @return validate result
	 */
	public static boolean validateTOTPCode(int authCode, String randomKey, long fixedTime) {
		return validateTOTPCode(CalcType.HmacSHA1, authCode, randomKey,
				fixedTime, Globals.DEFAULT_VALUE_INT, Globals.DEFAULT_VALUE_INT);
	}

	/**
	 * Validate auth code by given secret key, fixed time and fix window using default calculate type: HmacSHA1
	 *
	 * @param authCode  auth code
	 * @param randomKey random secret key
	 * @param fixedTime fixed time
	 * @param fixWindow fix window
	 * @return validate result
	 */
	public static boolean validateTOTPCode(int authCode, String randomKey, long fixedTime, int fixWindow) {
		return validateTOTPCode(CalcType.HmacSHA1, authCode, randomKey,
				fixedTime, Globals.DEFAULT_VALUE_INT, fixWindow);
	}

	/**
	 * Validate auth code by given secret key, fixed time, synchronize count and fix window
	 *
	 * @param calcType  Calculate type
	 * @param authCode  auth code
	 * @param randomKey random secret key
	 * @param fixedTime fixed time
	 * @param syncCount synchronize count
	 * @param fixWindow fix window
	 * @return validate result
	 */
	public static boolean validateTOTPCode(CalcType calcType, int authCode,
	                                       String randomKey, long fixedTime, int syncCount, int fixWindow) {
		if (authCode > Globals.INITIALIZE_INT_VALUE) {
			int minWindow = fixWindow < 0 ? (-1 * DEFAULT_WINDOW_SIZE) : (-1 * fixWindow);
			int maxWindow = fixWindow < 0 ? DEFAULT_WINDOW_SIZE : fixWindow;
			for (int i = minWindow ; i <= maxWindow ; i++) {
				int generateCode = generateTOTPCode(calcType, randomKey, fixedTime, syncCount, i);
				if (generateCode == authCode) {
					return true;
				}
			}
		}
		return Globals.DEFAULT_VALUE_BOOLEAN;
	}

	/**
	 * Validate auth code by given secret key and fixed time using default calculate type: HmacSHA1
	 *
	 * @param authCode   auth code
	 * @param randomKey  random secret key
	 * @param randomCode the random code
	 * @return validate result
	 */
	public static boolean validateHOTPCode(int authCode, String randomKey, long randomCode) {
		return authCode > Globals.INITIALIZE_INT_VALUE
				? authCode == generateHOTPCode(CalcType.HmacSHA1, randomKey, randomCode)
				: Globals.DEFAULT_VALUE_BOOLEAN;
	}

	/**
	 * Validate auth code by given secret key and fixed time using given calculate type
	 *
	 * @param authCode   auth code
	 * @param calcType   the calc type
	 * @param randomKey  random secret key
	 * @param randomCode the random code
	 * @return validate result
	 */
	public static boolean validateHOTPCode(int authCode, CalcType calcType, String randomKey, long randomCode) {
		return authCode > Globals.INITIALIZE_INT_VALUE
				? authCode == generateHOTPCode(calcType, randomKey, randomCode)
				: Globals.DEFAULT_VALUE_BOOLEAN;
	}

	/**
	 * Generate auth code
	 * @param calcType      Calculate type
	 * @param randomKey     random secret key
	 * @param fixedTime     fixed time
	 * @param syncCount     synchronize count
	 * @param fixWindow     fix window
	 * @return      generated code
	 */
	private static int generateTOTPCode(CalcType calcType, String randomKey,
	                                    long fixedTime, int syncCount, int fixWindow) {
		long currentGMTTime = DateTimeUtils.currentUTCTimeMillis();
		long calcTime = (currentGMTTime + fixedTime) / 1000L
				/ (syncCount == Globals.DEFAULT_VALUE_INT ? DEFAULT_SYNC_COUNT : syncCount);
		calcTime += fixWindow;
		return generateCode(calcType, randomKey, calcTime);
	}

	private static int generateCode(CalcType calcType, String randomKey, long calcTime) {
		byte[] signData = new byte[8];
		for (int i = 8 ; i-- > 0 ; calcTime >>>= 8) {
			signData[i] = (byte)calcTime;
		}
		byte[] secret = StringUtils.base32Decode(randomKey);
		byte[] hash;
		switch (calcType) {
			case HmacSHA1:
				hash = SecurityUtils.signDataByHmacSHA1(secret, signData);
				break;
			case HmacSHA256:
				hash = SecurityUtils.signDataByHmacSHA256(secret, signData);
				break;
			case HmacSHA512:
				hash = SecurityUtils.signDataByHmacSHA512(secret, signData);
				break;
			default:
				return Globals.DEFAULT_VALUE_INT;
		}
		int offset = hash[hash.length - 1] & 0xF;
		long resultCode = 0L;
		for (int i = 0 ; i < 4 ; ++i) {
			resultCode = (resultCode << 8) | (hash[offset + i] & 0xFF);
		}
		resultCode &= 0x7FFFFFFF;
		resultCode %= 1000000;
		return (int)resultCode;
	}

	/**
	 * The enum Calc type.
	 */
	public enum CalcType {
		/**
		 * Hmac sha 1 calc type.
		 */
		HmacSHA1,
		/**
		 * Hmac sha 256 calc type.
		 */
		HmacSHA256,
		/**
		 * Hmac sha 512 calc type.
		 */
		HmacSHA512
	}
}
