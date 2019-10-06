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
package com.nervousync.utils;

import java.util.Hashtable;
import java.util.Optional;

import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.core.Converter;

import com.nervousync.exceptions.beans.BeansException;

/**
 * @author Steven Wee	<a href="mailto:wmkm0113@Hotmail.com">wmkm0113@Hotmail.com</a>
 * @version $Revision: 1.0 $ $Date: Jun 25, 2015 2:55:15 PM $
 */
public final class BeanUtils {

	private static final Hashtable<String, BeanCopier> BEAN_COPIER_MAP = new Hashtable<>();

	private BeanUtils() {
		
	}
	
	/**
	 * Copy the property values of the given source bean into the target bean.
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param orig the source bean
	 * @param dest the target bean
	 * @throws BeansException if the copying failed
	 */
	public static void copyProperties(Object orig, Object dest) {
		BeanUtils.copyProperties(orig, dest, null);
	}
	
	/**
	 * Copy the property values of the given source bean into the target bean.
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 * @param orig the source bean
	 * @param dest the target bean
	 * @param converter converter instance
	 * @throws BeansException if the copying failed
	 */
	public static void copyProperties(Object orig, Object dest, Converter converter) {
		if (orig == null || dest == null) {
			return;
		}

		Optional<Converter> optional = Optional.ofNullable(converter);
		String cacheKey = BeanUtils.generateKey(orig.getClass(), dest.getClass(),
				optional.map(Converter::getClass).orElse(null));

		BeanCopier beanCopier;
		if (BeanUtils.BEAN_COPIER_MAP.containsKey(cacheKey)) {
			beanCopier = BeanUtils.BEAN_COPIER_MAP.get(cacheKey);
		} else {
			beanCopier = BeanCopier.create(orig.getClass(), dest.getClass(), optional.isPresent());
			BeanUtils.BEAN_COPIER_MAP.put(cacheKey, beanCopier);
		}

		beanCopier.copy(orig, dest, converter);
	}
	
	private static String generateKey(Class<?> origClass, Class<?> destClass, Class<?> converterClass) {
		if (converterClass == null) {
			return SecurityUtils.MD5(origClass.getName() + "To" + destClass.getName());
		} else {
			return SecurityUtils.MD5(origClass.getName() + "To" + destClass.getName() + "Converter:" + converterClass.getName());
		}
	}
}