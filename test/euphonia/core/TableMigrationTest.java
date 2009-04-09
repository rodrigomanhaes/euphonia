package euphonia.core;

import static org.junit.Assert.assertEquals;

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

import euphonia.core.transformation.Transformation;
import euphonia.test.JDBCUtil;

public class TableMigrationTest
{
	private Log log = LogFactory.getLog(TableMigrationTest.class);
	
	private static final String 
		DERBY_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver",
		DATABASE_SOURCE = "jdbc:derby:source;create=true",
		DATABASE_TARGET = "jdbc:derby:target;create=true";
	
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
			.from(DATABASE_SOURCE).in(DBMS.DERBY)
			.to(DATABASE_TARGET).in(DBMS.DERBY)
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
			.from(DATABASE_SOURCE).in(DBMS.DERBY)
			.to(DATABASE_TARGET).in(DBMS.DERBY)
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
		
		Transformation reverse = new Transformation() 
		{
			@Override
			public Object transform(Object input)
			{
				return reverse((String) input);
			}
		};
		
		new Migration()
			.from(DATABASE_SOURCE).in(DBMS.DERBY)
			.to(DATABASE_TARGET).in(DBMS.DERBY)
			.table("tb_pessoa").to("pessoa")
				.field("campo_id").to("id")
				.field("campo_nome").to("nome").withTransformation(reverse)
				.field("campo_cpf").to("cpf")
				.field("campo_identidade").to("identidade")
			.run();
		
		verifyMigrationOfEntireTableWithNames(
			reverse("Linus Torvalds"),
			reverse("Martin Fowler"),
			reverse("Kent Beck")
		);
	}
	
	@Test 
	public void shouldMigrateIncrementally() throws Exception
	{
		createAndPopulateSourceDatabase();
		createTargetDatabase("", "campo_", false);
		String sql = "insert into pessoa (campo_nome, campo_cpf, campo_identidade) values(?,?,?)";
		fillTable(target, sql, true);
		
		long sourceCount = JDBCUtil.recordCount(source, "tb_pessoa");
		long targetInitialCount = JDBCUtil.recordCount(target, "pessoa");
		
		new Migration()
			.from(DATABASE_SOURCE).in(DBMS.DERBY)
			.to(DATABASE_TARGET).in(DBMS.DERBY)
			.table("tb_pessoa").to("pessoa")
			.allFields()
			.incremental()
			.run();
		
		assertEquals(sourceCount + targetInitialCount, JDBCUtil.recordCount(target, "pessoa"));
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
	
	private void fillSourceTable(String sql) throws SQLException
	{
		fillTable(source, sql, false);
	}
	
	private void fillTable(Connection connection, String sql, boolean comId) throws SQLException
	{
		PreparedStatement ps = connection.prepareStatement(sql);
		
		try
		{
			int index = 0;
			if (comId)
				ps.setInt(++index, index);
			ps.setString(++index, "Linus Torvalds");
			ps.setString(++index, "12345678901");
			ps.setString(++index, "1214324234");
			ps.execute();
			ps.clearParameters();
			
			index = 0;
			if (comId)
				ps.setInt(++index, index);
			ps.setString(++index, "Martin Fowler");
			ps.setString(++index, "98765432109");
			ps.setString(++index, "984723948");
			ps.execute();
			ps.clearParameters();
			
			if (comId)
				ps.setInt(++index, index);
			ps.setString(++index, "Kent Beck");
			ps.setString(++index, "98787676565");
			ps.setString(++index, "768327362");
			
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
		names.add(paramNames != null ? paramNames[0] : "Linus Torvalds");
		cpfs.add("12345678901");
		identities.add("1214324234");
		
		ids.add(2);
		names.add(paramNames != null ? paramNames[1] : "Martin Fowler");
		cpfs.add("98765432109");
		identities.add("984723948");

		ids.add(3);
		names.add(paramNames != null ? paramNames[2] : "Kent Beck");
		cpfs.add("98787676565");
		identities.add("768327362");
		
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