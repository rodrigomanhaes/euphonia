package euphonia.core;

import static euphonia.core.database.DBMS.DERBY_EMBEDDED;
import static euphonia.core.transfer.TransferFactory.concat;
import static euphonia.core.transfer.TransferFactory.split;
import static euphonia.test.JDBCUtil.recordCount;
import static euphonia.util.CollectionUtil.array;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import euphonia.core.transfer.TransferStrategy;

public class TableMigrationTest
{
	private Log log = LogFactory.getLog(TableMigrationTest.class);
	
	private static final String 
		DERBY_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver",
		DATABASE_SOURCE = "jdbc:derby:temp/source;create=true",
		DATABASE_TARGET = "jdbc:derby:temp/target;create=true";
	
	private static Connection source, target;
	
	@BeforeClass
	public static void up() throws Exception
	{
		Class.forName(DERBY_JDBC_DRIVER);
		source = DriverManager.getConnection(DATABASE_SOURCE);
		target = DriverManager.getConnection(DATABASE_TARGET);
	}
	
	private void createTargetDatabase() throws SQLException
	{
		createTargetDatabase("", "");
	}
	
	private void createTargetDatabase(String tablePrefix, String attributePrefix) throws SQLException
	{
		createTargetDatabase(tablePrefix, attributePrefix, false);
	}
		
	private void createTargetDatabase(String tablePrefix, String attributePrefix, boolean identity)
		throws SQLException
	{
		String tableName = tablePrefix + "pessoa";
		Statement statement = target.createStatement();
		try
		{
			try
			{
				statement.execute("drop table " + tableName);
			}
			catch (SQLException e)
			{
				log.warn(e.getMessage());
			}
			statement.execute("create table " + tableName +
				"(" + attributePrefix + "id integer not null " + 
					(identity ? "generated always as identity " : "") +  ", " +
					  attributePrefix + "nome varchar(50) not null, " +
					  attributePrefix + "cpf varchar(11) not null, " +
					  attributePrefix + "identidade varchar(20) not null)");
		}
		finally
		{
			statement.close();
		}
	}

	private void createAndPopulateSourceDatabase() throws SQLException
	{
		Statement statement = source.createStatement();
		try
		{
			statement.execute("drop table tb_pessoa");
		}
		catch (SQLException e)
		{
			log.warn(e.getMessage());
		}
		
		try
		{
			String sql = "CREATE TABLE tb_pessoa (campo_id int not null generated always as identity, campo_nome varchar(40) not null, campo_cpf varchar(11) not null, campo_identidade varchar(20))";
			statement.execute(sql);
			
			sql = "insert into tb_pessoa (campo_nome, campo_cpf, campo_identidade) values(?,?,?)";
			fillSourceTable(sql);
		}
		catch (SQLException e)
		{
			log.warn(e.getMessage());
		}
		finally
		{
			statement.close();
		}
	}

	@Test
	public void shouldMigrateTable() throws Exception 
	{
		createAndPopulateSourceDatabase();
		createTargetDatabase();
		
		new Migration()
			.from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
			.to(DATABASE_TARGET).in(DERBY_EMBEDDED)
			.table("tb_pessoa").to("pessoa")
				.field("campo_id").to("id")
				.field("campo_nome").to("nome")
				.field("campo_cpf").to("cpf")
				.field("campo_identidade").to("identidade")
			.run();
		
		verifyMigrationOfEntireTable();
	}
	
	@Test
	public void shouldMigrateEntireTableWhenAllFieldsHaveSameNames()
		throws Exception
	{
		createAndPopulateSourceDatabase();
		createTargetDatabase("", "campo_");
		
		new Migration()
			.from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
			.to(DATABASE_TARGET).in(DERBY_EMBEDDED)
			.table("tb_pessoa").to("pessoa")
				.allFields()
			.run();
		
		verifyMigrationOfEntireTable("campo_");
	}
	
	@Test
	public void shouldMigrateTableWithTransformationOnField() throws Exception
	{
		createAndPopulateSourceDatabase();
		createTargetDatabase();
		
		TransferStrategy reverse = new TransferStrategy() 
		{
			@Override
			public Object[] transfer(Object... values) 
			{
				return array(reverse((String) values[0]));
			}
			
		};
		
		new Migration()
			.from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
			.to(DATABASE_TARGET).in(DERBY_EMBEDDED)
			.table("tb_pessoa").to("pessoa")
				.field("campo_id").to("id")
				.field("campo_nome").to("nome").withTransformation(reverse)
				.field("campo_cpf").to("cpf")
				.field("campo_identidade").to("identidade")
			.run();
		
		verifyMigrationOfEntireTableWithNames(
			reverse(TORVALDS_NAME),
			reverse(FOWLER_NAME),
			reverse(BECK_NAME)
		);
	}
	
	@Test 
	public void shouldMigrateIncrementally() throws Exception
	{
		createAndPopulateSourceDatabase();
		createTargetDatabase("", "campo_", false);
		String sql = "insert into pessoa (campo_id, campo_nome, campo_cpf, campo_identidade) values(?,?,?,?)";
		fillTable(target, sql, true);
		
		long sourceCount = recordCount(source, "tb_pessoa");
		long targetInitialCount = recordCount(target, "pessoa");
		assertEquals(3, sourceCount);
		assertEquals(3, targetInitialCount);
		
		new Migration()
			.from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
			.to(DATABASE_TARGET).in(DERBY_EMBEDDED)
			.table("tb_pessoa").to("pessoa")
				.allFields()
				.incremental()
			.run();
		
		assertEquals(sourceCount + targetInitialCount, recordCount(target, "pessoa"));
	}
	
	@Test
	public void shouldApplySelectionCondition() throws Exception
	{
		createAndPopulateSourceDatabase();
		createTargetDatabase("", "campo_");
		
		new Migration()
			.from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
			.to(DATABASE_TARGET).in(DERBY_EMBEDDED)
			.table("tb_pessoa").to("pessoa")
				.allFields()
				.where("campo_nome like '%Beck'")
			.run();
		
		assertEquals(1, recordCount(target, "pessoa"));
		compareRecord(target, "pessoa", 1, 
			array(
				array("campo_nome", BECK_NAME),
				array("campo_cpf", BECK_CPF),
				array("campo_identidade", BECK_IDENTIDADE)
			)
		);
	}
	
	@Test
	public void shouldMigrateManyFieldsToOne() throws SQLException
	{
		createAndPopulateSourceDatabase();
		dropAndCreateTable(target, "pessoa", 
			"create table pessoa " +
			"(id integer not null, " +
			" data varchar(255) not null)");
		
		new Migration()
			.from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
			.to(DATABASE_TARGET).in(DERBY_EMBEDDED)
			.table("tb_pessoa").to("pessoa")
				.field("campo_id").to("id")
				.fields("campo_nome", "campo_cpf", "campo_identidade").to("data")
					.withTransformation(concat(","))
			.run();
		
		compareRecord(target, "pessoa", 1,
			new String[][] {{"data", TORVALDS_NAME + "," + TORVALDS_CPF + "," + TORVALDS_IDENTIDADE}});
		compareRecord(target, "pessoa", 2, 
			new String[][] {{"data", FOWLER_NAME + "," + FOWLER_CPF + "," + FOWLER_IDENTIDADE}});
		compareRecord(target, "pessoa", 3, 
			new String[][] {{"data", BECK_NAME + "," + BECK_CPF + "," + BECK_IDENTIDADE}});
	}
	
	@Test
	public void shouldMigrateOneFieldToMany() throws SQLException
	{
		dropAndCreateTable(source, "tb_pessoa", 
			"create table tb_pessoa (" +
			"  id integer not null, " +
			"  data varchar(255) not null)");
		insertData(source, "tb_pessoa", 
			array(
				array("1", TORVALDS_NAME + ',' + TORVALDS_CPF + ',' + TORVALDS_IDENTIDADE),
				array("2", FOWLER_NAME + ',' + FOWLER_CPF + ',' + FOWLER_IDENTIDADE),
				array("3", BECK_NAME + ',' + BECK_CPF + ',' + BECK_IDENTIDADE)
			)
		);
		createTargetDatabase();
		
		new Migration()
			.from(DATABASE_SOURCE).in(DERBY_EMBEDDED)
			.to(DATABASE_TARGET).in(DERBY_EMBEDDED)
			.table("tb_pessoa").to("pessoa")
				.field("id").to("id")
				.fields("data").to("nome", "cpf", "identidade")
					.withTransformation(split(","))
			.run();
		
		compareRecord(target, "pessoa", 1,
			array(
				array("nome", TORVALDS_NAME),
				array("cpf", TORVALDS_CPF),
				array("identidade", TORVALDS_IDENTIDADE)
			)
		);
		compareRecord(target, "pessoa", 2,
			array(
				array("nome", FOWLER_NAME),
				array("cpf", FOWLER_CPF),
				array("identidade", FOWLER_IDENTIDADE)
			)
		);
		compareRecord(target, "pessoa", 3,
			array(
				array("nome", BECK_NAME),
				array("cpf", BECK_CPF),
				array("identidade", BECK_IDENTIDADE)
			)
		);
	}
		
	private void dropAndCreateTable(Connection connection, String tableName, String ddlCreate)
		throws SQLException
	{
		Statement statement = connection.createStatement();
		try
		{
			try
			{
				statement.execute("drop table " + tableName);
			}
			catch (SQLException e)
			{
				log.warn(e.getMessage());
			}
			statement.execute(ddlCreate);
		}
		finally
		{
			statement.close();
		}
	}
	
	private void compareRecord(Connection connection, String tableName, int recordNumber,
		String[][] camposeValores) throws SQLException
	{
		boolean found = false;
		Statement statement = connection.createStatement();
		try
		{
			ResultSet result = statement.executeQuery("select * from " + tableName);
			try
			{
				int index = 1;
				while (result.next() && !found)
				{
					if (index == recordNumber)
					{
						for (String[] campoValor: camposeValores)
							assertEquals(campoValor[1], result.getObject(campoValor[0]));
						found = true;
					}
					index++;
				}
			}
			finally
			{
				result.close();
			}
		}
		finally
		{
			statement.close();
		}
		assertTrue("Record with given index not found", found);
	}

	@AfterClass
	public static void down() throws Exception
	{
		source.close();
		target.close();
	}
	
	@Test
	public void shouldRevertString()
	{
		assertEquals("etseT mu he otsI", reverse("Isto eh um Teste"));
	}
	
	private String reverse(String input)
	{
		StringBuilder result = new StringBuilder();
		for (int index = input.length() - 1; index >= 0; index--)
			result.append(input.charAt(index));
		return result.toString();
	}
	
	private void insertData(Connection connection, String tableName, String[][] records)
		throws SQLException
	{
		StringBuilder sql = new StringBuilder()
			.append("insert into ")
			.append(tableName)
			.append(" values(");
		int fieldCount = records[0].length;
		for (int i = 1; i <= fieldCount; i++)
		{
			sql.append('?');
			if (i < fieldCount)
				sql.append(',');
		}
		sql.append(')');
		
		PreparedStatement ps = connection.prepareStatement(sql.toString());
		try
		{
			for (String[] record: records)
			{
				ps.clearParameters();
				int index = 1;
				for (String fieldValue: record)
					ps.setString(index++, fieldValue);
				ps.execute();
			}
		}
		finally
		{
			ps.close();
		}
	}

	
	private void fillSourceTable(String sql) throws SQLException
	{
		fillTable(source, sql, false);
	}
	
	private String
		TORVALDS_NAME = "Linus Torvalds",
		TORVALDS_CPF = "12345678901",
		TORVALDS_IDENTIDADE = "1214324234",
		FOWLER_NAME = "Martin Fowler",
		FOWLER_CPF = "98765432109",
		FOWLER_IDENTIDADE = "984723948",
		BECK_NAME = "Kent Beck",
		BECK_CPF = "98787676565",
		BECK_IDENTIDADE = "768327362"; 
	
	private void fillTable(Connection connection, String sql, boolean insertId) throws SQLException
	{
		PreparedStatement ps = connection.prepareStatement(sql);
		
		try
		{
			int index = 0;
			
			if (insertId)
				ps.setInt(++index, 1);
			ps.setString(++index, TORVALDS_NAME);
			ps.setString(++index, TORVALDS_CPF);
			ps.setString(++index, TORVALDS_IDENTIDADE);
			ps.execute();
			ps.clearParameters();
			
			index = 0;
			if (insertId)
				ps.setInt(++index, 2);
			ps.setString(++index, FOWLER_NAME);
			ps.setString(++index, FOWLER_CPF);
			ps.setString(++index, FOWLER_IDENTIDADE);
			ps.execute();
			ps.clearParameters();
			
			index = 0;
			if (insertId)
				ps.setInt(++index, 3);
			ps.setString(++index, BECK_NAME);
			ps.setString(++index, BECK_CPF);
			ps.setString(++index, BECK_IDENTIDADE);
			
			ps.execute();
			ps.clearParameters();
		}
		finally
		{
			ps.close();
		}
	}

	private void verifyMigrationOfEntireTable() throws SQLException
	{
		verifyMigrationOfEntireTable(null, "");
	}
	
	private void verifyMigrationOfEntireTable(String attributePrefix) throws SQLException
	{
		verifyMigrationOfEntireTable(null, attributePrefix);
	}
	
	private void verifyMigrationOfEntireTableWithNames(String... paramNames) throws SQLException
	{
		verifyMigrationOfEntireTable(paramNames, "");
	}
	
	private void verifyMigrationOfEntireTable(String[] paramNames, String attributePrefix) throws SQLException
	{
		Set<String> names = new HashSet<String>();
		Set<String> cpfs = new HashSet<String>();
		Set<Integer> ids = new HashSet<Integer>();
		Set<String> identities = new HashSet<String>();
		
		ids.add(1);
		names.add(paramNames != null ? paramNames[0] : TORVALDS_NAME);
		cpfs.add(TORVALDS_CPF);
		identities.add(TORVALDS_IDENTIDADE);
		
		ids.add(2);
		names.add(paramNames != null ? paramNames[1] : FOWLER_NAME);
		cpfs.add(FOWLER_CPF);
		identities.add(FOWLER_IDENTIDADE);

		ids.add(3);
		names.add(paramNames != null ? paramNames[2] : BECK_NAME);
		cpfs.add(BECK_CPF);
		identities.add(BECK_IDENTIDADE);
		
		Set<String> namesFound = new HashSet<String>();
		Set<String> cpfsFound = new HashSet<String>();
		Set<Integer> idsFound = new HashSet<Integer>();
		Set<String> identitiesFound = new HashSet<String>();
		
		ResultSet result = target.createStatement().executeQuery("select * from pessoa");
		try
		{
			while (result.next())
			{
				idsFound.add(result.getInt(attributePrefix + "id"));
				namesFound.add(result.getString(attributePrefix + "nome"));
				cpfsFound.add(result.getString(attributePrefix + "cpf"));
				identitiesFound.add(result.getString(attributePrefix + "identidade"));
			}
		}
		finally
		{
			result.close();
		}
		
		assertEquals(ids, idsFound);
		assertEquals(names, namesFound);
		assertEquals(cpfs, cpfsFound);
		assertEquals(identities, identitiesFound);
	}
}