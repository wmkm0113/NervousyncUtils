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
package com.nervousync.commons.zip.io.input;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.nervousync.commons.core.Globals;
import com.nervousync.commons.io.NervousyncRandomAccessFile;
import com.nervousync.commons.zip.ZipFile;
import com.nervousync.commons.zip.crypto.Decryptor;

/**
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: Dec 2, 2017 1:06:01 PM $
 */
public class InflaterInputStream extends PartInputStream {

	private final Inflater inflater;
	private final byte[] buffer;
	private final byte[] oneByteBuffer = new byte[1];
	private long writeBytes;
	private final long originalSize;
	
	public InflaterInputStream(ZipFile zipFile, NervousyncRandomAccessFile input, 
			long length, long originalSize, Decryptor decryptor, boolean isAESEncryptedFile) {
		super(zipFile, input, length, decryptor, isAESEncryptedFile);
		this.inflater = new Inflater(true);
		this.buffer = new byte[Globals.DEFAULT_BUFFER_SIZE];
		this.writeBytes = 0L;
		this.originalSize = originalSize;
	}
	
	public int read() throws IOException {
		return this.read(this.oneByteBuffer, 0, 1) == Globals.DEFAULT_VALUE_INT ? 
				Globals.DEFAULT_VALUE_INT : this.oneByteBuffer[0] & 0xFF;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		if (b == null) {
			throw new NullPointerException("Input buffer is null");
		}
		return this.read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException("Input buffer is null");
		} else if (off < 0 || len < 0 || off + len > b.length) {
			throw new IndexOutOfBoundsException();
		} else if (b.length == 0) {
			return 0;
		}
		
		try {
			if (this.writeBytes >= this.originalSize) {
				this.finishInflating();
				return Globals.DEFAULT_VALUE_INT;
			}
			
			int readLength;
			while ((readLength = this.inflater.inflate(b, off, len)) == 0) {
				if (this.inflater.finished() || this.inflater.needsDictionary()) {
					this.finishInflating();
					return Globals.DEFAULT_VALUE_INT;
				}

				if (this.inflater.needsInput()) {
					this.fill();
				}
			}
			
			this.writeBytes += readLength;
			return readLength;
		} catch (DataFormatException e) {
			throw new IOException("Invalid data format", e);
		}
	}

	@Override
	public long skip(long length) throws IOException {
		if (length < 0L) {
			throw new IllegalArgumentException("Negative skip length");
		}
		
		int limit = (int)Math.min(length, Integer.MAX_VALUE);
		int total = 0;
		byte[] b = new byte[512];
		while (total < limit) {
			int len = limit - total;
			if (len > b.length) {
				len = b.length;
			}
			len = this.read(b, 0, len);
			if (len == Globals.DEFAULT_VALUE_INT) {
				break;
			}
			total += len;
		}
		return total;
	}

	@Override
	public int available() {
		return this.inflater.finished() ? 0 : 1;
	}
	
	@Override
	public void close() throws IOException {
		this.inflater.end();
		super.close();
	}
	
	private void finishInflating() throws IOException {
		super.seekToEnd();
		this.checkAndReadAESMacBytes();
	}
	
	private void fill() throws IOException {
		int length = super.read(this.buffer, 0, this.buffer.length);
		if (length == Globals.DEFAULT_VALUE_INT) {
			throw new EOFException("Unexpected end of input stream");
		}
		this.inflater.setInput(this.buffer, 0, length);
	}
}