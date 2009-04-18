package euphonia.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import euphonia.core.database.DatabaseConnectionTest;
import euphonia.core.transfer.CaseTransformationTest;
import euphonia.core.transfer.TransferStrategyTest;

@RunWith(Suite.class)
@SuiteClasses({
	DatabaseConnectionTest.class,
	TableMigrationTest.class,
	CaseTransformationTest.class, 
	TransferStrategyTest.class
})
public class CoreSuite
{

}
