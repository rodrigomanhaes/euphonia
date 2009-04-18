package euphonia.core;

import static euphonia.util.CollectionUtil.array;
import static euphonia.util.CollectionUtil.stringRepresentation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import euphonia.core.database.ConnectionFactory;
import euphonia.core.database.DBMS;
import euphonia.core.database.DatabaseConnection;
import euphonia.core.transfer.TransferStrategy;

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
		return fields(array(name));
	}
	
	public Field fields(String... names) 
	{
		this.lastField = new Field(names, lastTable);
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
					runMigrationForTable(table, source, target);
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
	
	private void runMigrationForTable(Table table, DatabaseConnection source, DatabaseConnection target)
	{
		try
		{
			readDataFromSourceTable(table, source);
			if (!incremental)
				deleteFromTargetTable(table, target);
			writeDataToTargetTable(table, target);
		}
		catch (SQLException e)
		{
			throw new RuntimeException();
		}
	}
	
	private void readDataFromSourceTable(Table table, DatabaseConnection source)
		throws SQLException
	{
		ResultSet result = source.executeQuery(createSourceQuery(table));
		try
		{
			table.loadRecordsFromResultSet(result);
		}
		finally
		{
			result.close();
		}
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
						Object[] values = table.getValues(field, count-1); 
						log.debug("Including value " + stringRepresentation(values) + 
							" for field " + field + " in table " + table);
						ps.setObject(paramCount, field.copy(values)[0]);
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
			sql.append(field.targetNames()[0]).append(',');
		sql.delete(sql.length()-1, sql.length());
		sql.append(") VALUES(");
		for (int i = 0; i < table.fieldCount(); i++)
			sql.append("?,");
		sql.delete(sql.length()-1, sql.length());
		sql.append(')');
		log.debug(sql);
		return sql;
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

	public Migration withTransformation(TransferStrategy... transformations)
	{
		lastField.transfers(transformations);
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

}