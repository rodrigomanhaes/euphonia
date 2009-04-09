package euphonia.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	DatabaseConnectionTest.class,
	TableMigrationTest.class
})
public class CoreSuite
{

}
