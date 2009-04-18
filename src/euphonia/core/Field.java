package euphonia.core;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import euphonia.core.fields.FieldConversionManyToOne;
import euphonia.core.transformation.Transformation;

class Field
{
	String sourceName;
	String[] sourceNames;
	String sourceType;
	Migration migration;
	String targetName;
	Transformation transformation;
	FieldConversionManyToOne manyToOne;
	private Table table;
	
	public Field(String name, Table table, Migration migration)
	{
		this.sourceName = name;		
		this.migration = migration;
		this.table = table;
		table.addField(this);
	}
	
	public void getData(ResultSetMetaData metadata, Map<String, Integer> columns, ResultSet result) 
	{
		try
		{
			String[] fieldSourceNames = isManyToOne() ? sourceNames : new String[] {sourceName}; 
			for (String fieldSource: fieldSourceNames)
			{
				Integer index = columns.get(fieldSource.toUpperCase());
				sourceType = metadata.getColumnTypeName(index);
				table.putValue(fieldSource, result.getObject(fieldSource));
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e); 
		}
	}

	public void operation(FieldConversionManyToOne conversion) 
	{
		manyToOne = conversion;
	}

	private Field(String[] names, Table table, Migration migration)
	{
		this.sourceNames = names;		
		this.migration = migration;
		this.table = table;
		table.addField(this);
	}
	
	public static Field manyToOne(String[] names, Table lastTable, Migration migration) 
	{
		return new Field(names, lastTable, migration);
	}
	
	boolean isManyToOne()
	{
		return sourceNames != null;
	}

	public Object copy(Object value)
	{
		if (isManyToOne())
		{
			return manyToOne.convert((Object[]) value);
		}
		else
			return transformation == null ? value : transformation.transform(value);
	}

	public Field transformation(Transformation transformation)
	{
		this.transformation = transformation;
		return this;
	}
	
	public Transformation transformation()
	{
		return transformation;
	}

	public Migration to(String targetName)
	{
		this.targetName = targetName;
		return migration;
	}
	
	@Override
	public String toString()
	{
		String sourceName = null;
		if (isManyToOne())
		{
			StringBuilder builder = new StringBuilder()
				.append('[');
			for (String source: sourceNames)
				builder.append(source).append(',');
			sourceName = builder 
				.deleteCharAt(builder.length()-1)
				.append(']')
				.toString();
		}
		else
			sourceName = this.sourceName;
		
		return new StringBuilder()
			.append('(')
			.append(sourceName)
			.append(',')
			.append(targetName)
			.append(')')
			.toString();
	}
	
}