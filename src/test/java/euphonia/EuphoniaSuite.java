package euphonia;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import euphonia.core.CoreSuite;
import euphonia.util.StringUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
	CoreSuite.class,
	StringUtilTest.class
})
public class EuphoniaSuite
{

}
