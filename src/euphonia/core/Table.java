package euphonia.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Table
{
	String sourceName;
	private Migration migration;
	String targetName;
	private Map<String, List<Object>> sourceMap = new HashMap<String, List<Object>>();
	private List<Field> fields = new ArrayList<Field>();

	protected void addField(Field field)
	{
		fields.add(field);
		if (field.isManyToOne())
			for (String fieldName: field.sourceNames)
				sourceMap.put(fieldName, new ArrayList<Object>());
		else
			sourceMap.put(field.sourceName, new ArrayList<Object>());
	}

	protected int recordCount()
	{
		return sourceMap.get(sourceMap.keySet().iterator().next()).size();
	}
	
	protected int fieldCount()
	{
		return fields.size();
	}
	
	protected Iterable<Field> fields()
	{
		return fields;
	}

	protected Object getValue(String field, int index)
	{
		return sourceMap.get(field).get(index);
	}
	
	protected Object[] getValues(String[] fields, int index)
	{
		Object[] results = new Object[fields.length];
		for (int i = 0; i < fields.length; i++)
			results[i] = sourceMap.get(fields[i]).get(index);
		return results;
	}
	
	protected void putValue(String field, Object value)
	{
		sourceMap.get(field).add(value);
	}
	
	public Table(String name, Migration migration)
	{
		this.sourceName = name;		
		this.migration = migration;
		migration.addTable(this);
	}
	
	public Migration to(String targetName)
	{
		this.targetName = targetName;
		return migration;
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder()
			.append('(')
			.append(sourceName)
			.append(',')
			.append(targetName)
			.append(')')
			.toString();
	}
}
