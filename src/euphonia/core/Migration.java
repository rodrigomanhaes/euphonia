package euphonia.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import euphonia.core.database.ConnectionFactory;
import euphonia.core.database.DBMS;
import euphonia.core.database.DatabaseConnection;
import euphonia.core.fields.FieldConversionManyToOne;
import euphonia.core.transformation.Transformation;

public class Migration
{
	private static final Log log = LogFactory.getLog(Migration.class);
	
	private List<Table> tables = new ArrayList<Table>();
	private Table lastTable;
	private Field lastField;
	private boolean sourceWasLast = false;
	
	private boolean incremental = false;
	
	private String whereClause;
	
	private String sourceDatabase, targetDatabase;
	private DBMS sourceDBMS, targetDBMS;
	
	protected boolean addTable(Table table)
	{
		return tables.add(table);
	}

	public Table table(String name)
	{
		Table table = new Table(name, this);
		lastTable = table;
		return table;
	}

	public Field field(String name)
	{
		this.lastField = new Field(name, lastTable, this); 
		return lastField;
	}
	
	public Field fields(String... names) 
	{
		this.lastField = Field.manyToOne(names, lastTable, this);
		return lastField;
	}
	
	public Migration run()
	{
		DatabaseConnection source = ConnectionFactory.getConnection(sourceDBMS)
			.open(sourceDatabase, null, null);
		try
		{
			DatabaseConnection target = ConnectionFactory.getConnection(targetDBMS)
				.open(targetDatabase, null, null);

			try
			{
				for (Table table: tables)
					runTable(source, target, table);
			}
			finally
			{
				target.close();
			}
		}
		finally
		{
			source.close();
		}
		
		return this;
	}
	
	private void runTable(DatabaseConnection source, DatabaseConnection target, Table table)
	{
		readDataFromSourceTable(table, source);
		if (!incremental)
			deleteFromTargetTable(table, target);
		writeDataToTargetTable(table, target);
	}

	private void writeDataToTargetTable(Table table, DatabaseConnection target)
	{
		StringBuilder sql = createInsertQuery(table);
		
		try
		{
			PreparedStatement ps = target.getConnection().prepareStatement(sql.toString());
			try
			{
				for (int count = 1; count <= table.recordCount(); count++)
				{
					ps.clearParameters();
					int paramCount = 1;
					for (Field field: table.fields())
					{
						Object value = field.isManyToOne() ?
								table.getValues(field.sourceNames, count-1) : 
								table.getValue(field.sourceName, count-1); 
						log.debug("Including value " + value + 
							" for field " + field + " in table " + table);
						ps.setObject(paramCount, field.copy(value));
						paramCount++;
					}
					ps.execute();
				}
			}
			finally
			{
				ps.close();
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}


	private StringBuilder createInsertQuery(Table table)
	{
		StringBuilder sql = new StringBuilder()
			.append("insert into ")
			.append(table.targetName)
			.append(" (");
		
		for (Field field: table.fields())
			sql.append(field.targetName).append(',');
		sql.delete(sql.length()-1, sql.length());
		sql.append(") VALUES(");
		for (int i = 0; i < table.fieldCount(); i++)
			sql.append("?,");
		sql.delete(sql.length()-1, sql.length());
		sql.append(')');
		log.debug(sql);
		return sql;
	}


	private void readDataFromSourceTable(Table table, DatabaseConnection source)
	{
		try
		{
			ResultSet result = source.executeQuery(createSourceQuery(table));
			try
			{
				ResultSetMetaData metadata = result.getMetaData();
				Map<String, Integer> columns = loadColumns(metadata);
				while (result.next())
				{
					for (Field field: table.fields())
						field.getData(metadata, columns, result);
				}
			}
			finally
			{
				result.close();
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}


	private String createSourceQuery(Table table) 
	{
		StringBuilder builder = new StringBuilder()
			.append("select * from ")
			.append(table.sourceName); 
		
		if (whereClause != null)
			builder
				.append(" where ")
				.append(whereClause);
		
		return builder.toString();
	}

	private Map<String, Integer> loadColumns(ResultSetMetaData metadata)
		throws SQLException
	{
		Map<String, Integer> columns = new HashMap<String, Integer>();
		for (int i = 1; i <= metadata.getColumnCount(); i++)
			columns.put(metadata.getColumnName(i).toUpperCase(), i);
		return columns;
	}

	private void deleteFromTargetTable(Table table, DatabaseConnection target)
	{
		target.execute("delete from " + table.targetName);
	}

	public Migration from(String databaseSource)
	{
		this.sourceDatabase = databaseSource;
		sourceWasLast = true;
		return this;
	}


	public Migration to(String databaseTarget)
	{
		this.targetDatabase = databaseTarget;
		sourceWasLast = false;
		return this;
	}


	public Migration in(DBMS sgbd)
	{
		if (sourceWasLast)
			this.sourceDBMS = sgbd;
		else
			this.targetDBMS = sgbd;
		return this;
	}

	public Migration allFields()
	{
		try
		{
			DatabaseConnection source = ConnectionFactory.getConnection(sourceDBMS)
				.open(sourceDatabase, null, null);
			try
			{
				String tableName = lastTable.sourceName;
				ResultSet result = source.executeQuery("select * from " + tableName);
				try
				{
					ResultSetMetaData metadata = result.getMetaData();
					for (int i = 1; i <= metadata.getColumnCount(); i++)
					{
						String columnName = metadata.getColumnName(i);
						this.field(columnName).to(columnName);
					}
				}
				finally
				{
					result.close();
				}
			}
			finally
			{
				source.close();
			}
			return this;
			
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}

	public Migration withTransformation(Transformation transformation)
	{
		lastField.transformation(transformation);
		return this;
	}

	public Migration incremental()
	{
		incremental = true;
		return this;
	}

	public Migration where(String whereClause) 
	{
		this.whereClause = whereClause;
		return this;
	}

	public Migration operation(FieldConversionManyToOne conversion) 
	{
		lastField.operation(conversion);
		return this;
	}
}