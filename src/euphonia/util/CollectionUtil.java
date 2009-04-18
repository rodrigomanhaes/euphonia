package euphonia.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class CollectionUtil 
{
	public static String stringRepresentation(Object[] array)
	{
		return stringRepresentation(Arrays.asList(array));
	}
	
	public static String stringRepresentation(Collection<?> collection)
	{
		if (collection == null)
			return null;
		if (collection.isEmpty())
			return "";
		
		Iterator<?> iterator = collection.iterator();
		if (collection.size() == 1)
			return iterator.next().toString();
			
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		while (iterator.hasNext())
		{
			builder.append(iterator.next());
			if (iterator.hasNext())
				builder.append(", ");
		}
		builder.append(']');
		return builder.toString();
	}
	
	public static boolean empty(Object[] array)
	{
		return array == null || array.length == 0;
	}
	
	public static boolean empty(Collection<?> collection)
	{
		return collection == null || collection.isEmpty();
	}
	
	public static boolean empty(Map<?, ?> map)
	{
		return map == null || map.isEmpty();
	}
	
	public static <T> T[] array(T... objects)
	{
		return objects;
	}
}