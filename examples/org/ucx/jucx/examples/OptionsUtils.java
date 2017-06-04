package org.ucx.jucx.examples;

import java.util.HashMap;
import java.util.Map;

public class OptionsUtils {
	
	public static Map<String, String> getOptionsMap(String[] args, int whence) {
		Map<String, String> parameters = new HashMap<>();
		for (int i = whence; i < args.length; i++) {
			if (i % 2 == whence)
			{
				String val;
				switch (args[i])
				{
				case "-h":
				case "-v":
					val = "";
					break;
					
				default:
					val = args[i + 1];
					break;
				}
				parameters.put(args[i], val);
			}
		}
		
		return parameters;
	}
	
	public static Map<String, String> getOptionsMap(String[] args) {
		return getOptionsMap(args, 0);
	}
}
