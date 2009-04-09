package euphonia.util;

import java.util.Arrays;
import java.util.List;

public class StringUtil
{

	private static final String[] NON_CAPITALIZABLE_WORDS = 
	{ // portugues
	  "de", "do", "dos", "das", "e", "um", "uma", "uns", "umas",
	  // english
	  "of", "a", "an", "and",
	  // espanol
	  "y", "el", "los", "las", "del",
	  // deutsche?
	  "von", "van", "der" 
	};
	
	private static final List<String> NON_CAPITALIZABLE_LIST = 
		Arrays.asList(NON_CAPITALIZABLE_WORDS);
	
	public static String capitalize(String input)
	{
		String[] words = input.split(" ");
		StringBuilder result = new StringBuilder();
		for (String word: words)
		{
			if (result.length() > 0)
				result.append(' ');
			if (NON_CAPITALIZABLE_LIST.contains(word.toLowerCase()) && result.length() > 0)
				result.append(word.toLowerCase());
			else if (word.length() == 1 && result.length() > 0)
				result.append(word.toLowerCase());
			else
				result
					.append(Character.toUpperCase(word.charAt(0)))
					.append(word.substring(1, word.length()).toLowerCase());
		}
		return result.toString();
	}

}
