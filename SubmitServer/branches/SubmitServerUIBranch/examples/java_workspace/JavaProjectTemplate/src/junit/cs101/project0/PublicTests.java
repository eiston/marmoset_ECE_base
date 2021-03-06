package cs101.project0;

import junit.framework.TestCase;

/**
 * Add Public Tests to this class. Public tests are written by the Instructor
 * and may be used for grading projects. This source code is made available to
 * students.
 */
public class PublicTests extends TestCase {

	/**
	 * Optionally set up any member variables. This method is called before each
	 * test is run.
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Optionally close any open streams or connections. This method is called
	 * after each test is run.
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Each test case is "public void" and has a name that starts with "test"
	 * Use descriptive names because these names are given to students in Submit
	 * Server.
	 */
	public void testSomething() {
		// a test case should pass unless an exception is thrown or an
		// "assertion" fails. For example, any of the following conditions will
		// fail this test:
		fail("This test has failed");
		assertTrue(5 == 7);
		assertFalse(5 == 5);
		assertEquals(5, 7);
	}

}
