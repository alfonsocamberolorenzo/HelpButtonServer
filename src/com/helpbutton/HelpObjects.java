package com.helpbutton;

import java.util.HashMap;
import java.util.Map;
/**
 * Class HelpObjects implements a HashMap of Integer and Help objects in order to store all the Help objects
 * during the request linked to them is in process. Using the Integer reference, the same object can be referenced
 * from the outside. 
 * 
 * @author Alfonso Cambero Lorenzo
 * @version 1.0
 * @since 2014-02-01
 */
public class HelpObjects {
	public static Map<Integer,Help> helpObjects = new HashMap<Integer,Help>();
}
