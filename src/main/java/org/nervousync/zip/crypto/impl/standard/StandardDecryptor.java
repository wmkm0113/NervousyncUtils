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
package org.nervousync.zip.crypto.impl.standard;

import org.nervousync.commons.core.zip.ZipConstants;
import org.nervousync.zip.engine.ZipCryptoEngine;
import org.nervousync.zip.models.header.LocalFileHeader;
import org.nervousync.exceptions.zip.ZipException;
import org.nervousync.zip.crypto.Decryptor;

/**
 * Decryptor implement of standard
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: Dec 2, 2017 12:23:51 PM $
 */
public class StandardDecryptor implements Decryptor {

	private final ZipCryptoEngine zipCryptoEngine;
	
	public StandardDecryptor(LocalFileHeader localFileHeader, byte[] decryptorHeader)
			throws ZipException {
		if (localFileHeader == null) {
			throw new ZipException("General file header is null!");
		}

		this.zipCryptoEngine = new ZipCryptoEngine();

		if (localFileHeader.getPassword() == null
				|| localFileHeader.getPassword().length == 0) {
			throw new ZipException("Wrong password");
		}
		
		this.zipCryptoEngine.initKeys(localFileHeader.getPassword());

		int result = decryptorHeader[0];
		for (int i = 0; i < ZipConstants.STD_DEC_HDR_SIZE ; i++) {
			this.zipCryptoEngine.updateKeys((byte)(result ^ this.zipCryptoEngine.decryptByte()));
			if ((i + 1) != ZipConstants.STD_DEC_HDR_SIZE) {
				result = decryptorHeader[i + 1];
			}
		}
	}

	@Override
	public int decryptData(byte[] buff) throws ZipException {
		return this.decryptData(buff, 0, buff.length);
	}
	
	@Override
	public int decryptData(byte[] buff, int start, int len) throws ZipException {
		if (start < 0 || len < 0) {
			throw new ZipException("input argument error!");
		}
		
		try {
			for (int i = start ; i < start + len ; i++) {
				int value = buff[i] & 0xFF;
				value = (value ^ this.zipCryptoEngine.decryptByte()) & 0xFF;
				this.zipCryptoEngine.updateKeys((byte)value);
				buff[i] = (byte)value;
			}
			return len;
		} catch (Exception e) {
			if (e instanceof ZipException) {
				throw (ZipException)e;
			} else {
				throw new ZipException(e);
			}
		}
	}

}
