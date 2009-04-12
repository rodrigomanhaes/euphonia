package euphonia;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import euphonia.core.CoreSuite;
import euphonia.core.fields.FieldConversionManyToOneTest;
import euphonia.core.transformation.CaseTransformationTest;
import euphonia.util.StringUtilTest;

@RunWith(Suite.class)
@SuiteClasses({
	CoreSuite.class,
	CaseTransformationTest.class,
	FieldConversionManyToOneTest.class,
	StringUtilTest.class
})
public class EuphoniaSuite
{

}
