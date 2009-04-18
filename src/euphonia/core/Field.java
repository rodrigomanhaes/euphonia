package euphonia.core;

import static euphonia.util.CollectionUtil.empty;
import static euphonia.util.CollectionUtil.stringRepresentation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import euphonia.core.transfer.TransferStrategy;

/**
 * A field is a migration unit within a table. A field can be composed by more than one physical field 
 * for both input and target.
 * 
 * @author Rodrigo Manh&atilde;es
 */
class Field
{
	private String[] sourceNames;
	private String[] targetNames;
	private TransferStrategy[] transferers;
	private Table table;
	
	public Field(String[] names, Table table)
	{
		this.sourceNames = names;		
		this.table = table;
		table.addField(this);
	}
	
	public void appendRecordFromResultSetToTable(ResultSet result, Map<String, Integer> columns) 
	{
		try
		{
			for (String fieldSource: sourceNames)
				table.putValue(fieldSource, result.getObject(fieldSource));
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e); 
		}
	}

	public void transfer(TransferStrategy transferer) 
	{
		transfers(transferer);
	}

	public Object[] copy(Object[] values)
	{
		return empty(transferers) ? values : this.applyTransfers(values);
	}

	private Object[] applyTransfers(Object[] values) 
	{
		Object[] results = values;
		for (TransferStrategy transferer: transferers)
			results = transferer.transfer(results);
		return results;
	}

	public Migration to(String... targetNames)
	{
		this.targetNames = targetNames;
		return table.migration();
	}
	
	public boolean isSingle()
	{
		return sourceNames.length == 0;
	}
	
	@Override
	public String toString()
	{
		return new StringBuilder()
			.append('(')
			.append(stringRepresentation(sourceNames))
			.append(',')
			.append(stringRepresentation(targetNames))
			.append(')')
			.toString();
	}

	public void putEntriesForSourceParts(Map<String, List<Object>> sourceMap) 
	{
		for (String fieldName: sourceNames)
			sourceMap.put(fieldName, new ArrayList<Object>());
	}

	public Object[] getValues(Map<String, List<Object>> sourceMap, int index) 
	{
		Object[] results = new Object[sourceNames.length];
		for (int i = 0; i < sourceNames.length; i++)
			results[i] = sourceMap.get(sourceNames[i]).get(index);
		return results;
	}

	public String[] targetNames()
	{
		return targetNames;
	}

	public void transfers(TransferStrategy... transferers) 
	{
		this.transferers = transferers;
	}
	
}