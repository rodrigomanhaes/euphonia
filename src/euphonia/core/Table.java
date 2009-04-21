package euphonia.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
		field.putEntriesForSourceParts(sourceMap);
	}

	protected int recordCount()
	{
		return sourceMap.get(sourceMap.keySet().iterator().next()).size();
	}
	
	protected Iterable<Field> fields()
	{
		return fields;
	}

	protected Object[] getValues(Field field, int index)
	{
		return field.getValues(this.sourceMap, index);
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

	public void loadRecordsFromResultSet(ResultSet result) 
	{
		try
		{
			ResultSetMetaData metadata = result.getMetaData();
			Map<String, Integer> columns = loadMapOfColumnNamesAndNumbers(metadata);
			while (result.next())
			{
				for (Field field: fields())
					field.appendRecordFromResultSetToTable(result, columns);
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private Map<String, Integer> loadMapOfColumnNamesAndNumbers(ResultSetMetaData metadata)
		throws SQLException
	{
		Map<String, Integer> columns = new HashMap<String, Integer>();
		for (int columnNumber = 1; columnNumber <= metadata.getColumnCount(); columnNumber++)
			columns.put(metadata.getColumnName(columnNumber).toUpperCase(), columnNumber);
		return columns;
	}
	
	public Migration migration()
	{
		return migration;
	}
}
