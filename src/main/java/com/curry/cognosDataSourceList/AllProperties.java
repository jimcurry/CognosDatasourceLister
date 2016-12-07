package com.curry.cognosDataSourceList;
/**
 * Licensed Materials - Property of IBM
 * 
 * IBM Cognos Products: SDK Support
 * 
 * (c) Copyright IBM Corp. 2010, 2014
 * 
 * US Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 */

import com.cognos.developer.schemas.bibus._3.PropEnum;

import java.lang.reflect.Field;
import java.util.Vector;

/**
 * AllProperties.java
 * 
 * Description: Technote 1503137 SDK Sample - Getting all the properties of an Object in the PropEnum class.
 * 
 * Base Installation : IBM Cognos 10.2.x
 * Tested with : IBM JDK 6.0
 * Modified Date : 141114
 * 
 * Utilization in code : PropEnum props[] = AllProperties.getProperties();
 * 
 */


public class AllProperties {

	/**
	 * Get all the available properties in the PropEnum class
	 * 
	 * @return the array of PropEnum
	 * 
	 */
	static public PropEnum[] getProperties() {
		Vector<PropEnum> v = new Vector<PropEnum>();

		try {
			Class<?> cls = Class.forName("com.cognos.developer.schemas.bibus._3.PropEnum");

			Field fieldlist[] = cls.getDeclaredFields();
			for (int i = 0; i < fieldlist.length; i++) {
				Field fields = fieldlist[i];
				String type = fields.getType().getName();
				String name = fields.getName();
				// We need only the fields of the type String,
				// except for the field with the name "-value_"
				if (name.indexOf("_value_") == -1 && type.indexOf("String") > -1) {
					name = name.substring(1);
					if (isFound(name)) {
						v.add(PropEnum.fromString(name));
					}
				}
			}

			// Create the returned array of properties
			int len = v.size();
			PropEnum[] result = new PropEnum[len];
			for (int i = 0; i < len; i++) {
				result[i] = v.elementAt(i);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Returns true if the property name is not in the list of not found properties
	 * 
	 * @param name
	 *            the name of the property
	 * @return true if the property name is not on the list of not found properties
	 * 
	 *         This is the exclusions list of properties that can no longer be requested.
	 * 
	 */
	private static boolean isFound(String name) {
		// The following fields cannot be found when used in a search
		String notFoundFields[] = { "addedObjectCount", "deletedObjectCount", "migratedObject", "replacedObjectCount",
				"updatedObjectCount", "stsAffineConnections" };
		for (int j = 0; j < notFoundFields.length; j++) {
			if (name.equals(notFoundFields[j])) {
				return false;
			}
		}
		return true;
	}
}
