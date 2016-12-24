/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Sep 1, 2004
 */
package edu.umd.cs.marmoset.modelClasses;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import org.dom4j.DocumentException;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.marmoset.codeCoverage.CodeCoverageResults;
import edu.umd.cs.marmoset.codeCoverage.FileWithCoverage;

/**
 * A collection of TestOutcomes. These are associated with a specific
 * testRunPK; this class should not be used to store test outcomes from
 * different testRuns.  There is no check for this specifically, though.
 * 
 * TODO: make this implement the List interface and delegate calls to the 
 * testOutcomes object.  Then JSPs can iterate over this class directly.
 * 
 * TODO This code is disgusting.  Refactor this!
 * 
 * @author David Hovemeyer
 * @author jspacco
 */

public class TestOutcomeCollection implements ITestSummary, Iterable<TestOutcome> {
    private List<TestOutcome> testOutcomes = new ArrayList<TestOutcome>();
    private Map<String, TestOutcome> testMap = new HashMap<String, TestOutcome>();
    private boolean compileSuccessful = false;
    private String testRunPK;

    public static String formattedColumnHeaders(int numCols, TestOutcomeCollection canonicalResults)
    {
    	if (canonicalResults == null) return "";
		StringBuffer buf = new StringBuffer();
		List<TestOutcome> publicOutcomes = canonicalResults.getPublicOutcomes();
		List<TestOutcome> releaseOutcomes = canonicalResults.getReleaseOutcomes();
		List<TestOutcome> secretOutcomes = canonicalResults.getSecretOutcomes();
		
		for (int ii=0; ii<numCols; ii++) {
			buf.append("<col>");
		}
		
		// col.right adds a dark line to the right border of the cells that divide the different
		// categories of tests
		if (!publicOutcomes.isEmpty()) {
			for (int ii=0; ii<publicOutcomes.size()-1;ii++) {
				buf.append("<col>");
			}
			buf.append("<col class=\"right\">");
		}
		if (!releaseOutcomes.isEmpty()) {
			for (int ii=0; ii<releaseOutcomes.size()-1;ii++) {
				buf.append("<col>");
			}
			buf.append("<col class=\"right\">");
		}
		if (!secretOutcomes.isEmpty()) {
			for (int ii=0; ii<secretOutcomes.size()-1;ii++) {
				buf.append("<col>");
			}
		}
		return buf.toString();
    }
    
    public static String formattedTestHeaderTop(TestOutcomeCollection canonicalResults)
    {
        if (canonicalResults == null) return "";
		StringBuffer buf = new StringBuffer();
		List<TestOutcome> publicOutcomes = canonicalResults.getPublicOutcomes();
		List<TestOutcome> releaseOutcomes = canonicalResults.getReleaseOutcomes();
		List<TestOutcome> secretOutcomes = canonicalResults.getSecretOutcomes();
		
		if (!publicOutcomes.isEmpty()) {
			buf.append("<th colspan=\""
					+ publicOutcomes.size()
					+ "\">Public</th>");
		}
		if (!releaseOutcomes.isEmpty()) {
			buf.append("<th colspan=\""
					+ releaseOutcomes.size()
					+ "\">Release</th>");
		}
		if (!secretOutcomes.isEmpty()) {
			buf.append("<th colspan=\""
					+ secretOutcomes.size()
					+ "\">Secret</th>");
		}
		return buf.toString();
	}

	public static String formattedTestHeader(
			TestOutcomeCollection canonicalResults) {
	    if (canonicalResults == null) return "";
		StringBuffer buf = new StringBuffer();

		if (!canonicalResults.getPublicOutcomes().isEmpty()) {
			int counter = 0;
			for (Iterator<TestOutcome> i = canonicalResults.getPublicOutcomes().iterator(); i
					.hasNext(); counter++) {
				TestOutcome test = i.next();
				buf.append("<th><a title=\"" + test.getShortTestName()+"\">" + counter + "</a></th>");
			}
		}

		if (!canonicalResults.getReleaseOutcomes().isEmpty()) {
			int counter = 0;
			for (Iterator<TestOutcome> i = canonicalResults.getReleaseOutcomes().iterator(); i
					.hasNext(); counter++) {
				TestOutcome test = i.next();
                buf.append("<th><a title=\"" + test.getShortTestName()+"\">" + counter + "</a></th>");
			}
		}

		if (!canonicalResults.getSecretOutcomes().isEmpty()) {
			int counter = 0;
			for (Iterator<TestOutcome> i = canonicalResults.getSecretOutcomes().iterator(); i
					.hasNext(); counter++) {
				TestOutcome test = i.next();
                buf.append("<th><a title=\"" + test.getShortTestName()+"\">" + counter + "</a></th>");
			}
		}
		return buf.toString();
	}

	/**
	 * Returns HTML formatted for a table using a cascading style sheet.
	 * If results == null then the test results are not ready, and we color
	 * the table background the same if the tests could_not_run.
	 * @param canonicalResults
	 * @param results
	 * @return
	 */
	public static String formattedTestResults(TestOutcomeCollection canonicalResults,
            TestOutcomeCollection results) {
        if (canonicalResults == null)
            return "";
        StringBuffer buf = new StringBuffer();

        if (!canonicalResults.getPublicOutcomes().isEmpty()) {
            if (results == null || results.getCouldNotRunPublicTests()) {
                buf.append("<td class=\"could_not_run\" colspan=\""
                        + canonicalResults.getPublicOutcomes().size() + "\">&nbsp;</td>");
            } else {
                List<TestOutcome> tests = canonicalResults.getPublicOutcomes();
                formatTestResults(results, buf, tests);
            }
        }

        if (!canonicalResults.getReleaseOutcomes().isEmpty()) {
            if (results == null || results.getCouldNotRunReleaseTests()) {
                buf
                        .append("<td class=\"could_not_run\" colspan=\""
                                + canonicalResults.getReleaseOutcomes().size()
                                + "\">&nbsp;</td>");
            } else {
                List<TestOutcome> tests = canonicalResults.getReleaseOutcomes();
                formatTestResults(results, buf, tests);
            }
        }

        if (!canonicalResults.getSecretOutcomes().isEmpty()) {
            if (results == null || results.getCouldNotRunSecretTests()) {
                buf.append("<td class=\"could_not_run\" colspan=\""
                        + canonicalResults.getSecretOutcomes().size() + "\">&nbsp;</td>");
            } else {
                List<TestOutcome> tests = canonicalResults.getSecretOutcomes();
                formatTestResults(results, buf, tests);
            }
        }

        return buf.toString();
    }

    /**
     * @param results
     * @param buf
     * @param tests
     */
    private static void formatTestResults(TestOutcomeCollection results, StringBuffer buf, List<TestOutcome> tests) {
        for (Iterator<TestOutcome> i = tests.iterator(); i.hasNext();) {
            TestOutcome canonicalResult = i.next();
            TestOutcome test = results.getTest(canonicalResult.getTestName());
            if (test != null)
                buf.append("<td class=\"" + test.getOutcome() + "\"><a title=\""
                        + test.getShortTestName() + "\">&nbsp;</a></td>");
            else {
                buf.append("<td class=\"could_not_run\"><a title=\""
                        + canonicalResult.getShortTestName() + "\">&nbsp;</td>");
            }
        }
    }
    /**
	 * Checks if the collection is empty.
	 * 
	 * @return true if the collection is empty; false otherwise.
	 */
    public boolean isEmpty() {
        return testOutcomes.isEmpty();
    }
    
    /**
     * Returns a compact summary of the public, release and secret test scores
     * suitable to be displayed on a web-page.
     * @return A summary of the public, release and secret scores for this project.
     */
    public String getCompactOutcomesSummary() {
        return getValuePublicTestsPassed() +" / " +
            getValueReleaseTestsPassed() +" / " +
            getValueSecretTestsPassed();
    }
    /**
     * Returns a more detailed summary consisting of the public, release and secret scores, 
     * along with the number of FindBugs warnings and student-written tests, to be displayed 
     * on a web page.  Currenly only Java supports FindBugs warnings and student-written JUnit
     * tests.
     * @return A detailed summary of the scores that includes the number of FindBugs warnings
     * and the number of student-written tests.
     */
    public String getJavaOutcomesSummary() {
        return getCompactOutcomesSummary() +" / "+
            getNumFindBugsWarnings() +" / "+
            getNumStudentWrittenTests();
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getValuePassedOverall()
     */
    public int getValuePassedOverall() {
        // if the build_test returned COULD_NOT_RUN, then return 0 for the
        // overall
        if (!isCompileSuccessful())
            return 0;
        int publicValue = getValuePublicTestsPassed();
        int releaseValue = getValueReleaseTestsPassed();
        int secretValue = getValueSecretTestsPassed();
        if (publicValue < 0) publicValue=0;
        if (releaseValue < 0) releaseValue=0;
        if (secretValue < 0) secretValue=0;
        return publicValue + releaseValue + secretValue;
    }

    /**
     * Get number of release tests in this collection.
     * 
     * @return number of release tests in this collection
     */
    public int getValueReleaseTests() {
        return scoreOutcomes(getReleaseOutcomes());
    }

    /**
     * Get number of public tests for this collection.
     * 
     * @return number of public tests for this collection.
     */
    public int getValuePublicTests() {
        return scoreOutcomes(getOutcomesForTestType(TestOutcome.PUBLIC_TEST));
    }



    /**
     * Get number of failed tests.
     * @return number of failed tests
     */
    public int getValueFailedOverall() {
        return scoreNonPassedOutcomes();
    }

    /**
     * Get number of passed tests.
     * @return number of passed tests
     */
    public int getNumPassedOverall() {
        int numPassed=0;
        for (TestOutcome outcome : getAllTestOutcomes()) {
            if (outcome.isPassed())
                numPassed++;
        }
        return numPassed;
    }
    
    public int getValueSecretTests() {
        return scoreOutcomes(getOutcomesForTestType(TestOutcome.SECRET_TEST));
    }
    
    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getValueSecretTestsPassed()
     */
    public int getValueSecretTestsPassed() {
        List<TestOutcome> secretTests = getSecretOutcomes();
        return scoreOutcomes(TestOutcome.PASSED, secretTests);
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getValueReleaseTestsPassed()
     */
    public int getValueReleaseTestsPassed() {
        List<TestOutcome> releaseTests = getReleaseOutcomes();
        return scoreOutcomes(TestOutcome.PASSED, releaseTests);
    }


    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getValuePublicTestsPassed()
     */
    public int getValuePublicTestsPassed() {
        List<TestOutcome> publicTests = getOutcomesForTestType(TestOutcome.PUBLIC_TEST);
        return scoreOutcomes(TestOutcome.PASSED, publicTests);
    }
    
    /**
     * @return True if this collection has passed all the public tests;
     * false otherwise.
     */
    public boolean getPassedAllPublicTests() {
        List publicTests = getOutcomesForTestType(TestOutcome.PUBLIC_TEST);
        return (publicTests.size() == countOutcomes(TestOutcome.PASSED, getPublicOutcomes()));
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#isCompileSuccessful()
     */
    public boolean isCompileSuccessful() {
        return compileSuccessful;
    }

    public int getNumMethods()
    {
        return countOutcomes(TestOutcome.METHOD_TEST);
    }
    
    public int getNumOpcodes()
    {
        return countOutcomes(TestOutcome.OPCODE_TEST);
    }
    
    public int getNumClasses()
    {
        return countOutcomes(TestOutcome.CLASS_TEST);
    }
    
    /**
     * Get number of failed tests.
     * @return number of failed tests
     */
    public int getNumFailedOverall() {
        return countNonPassedOutcomes();
    }
    
    /**
     * Get the number of student-written tests.
     * @return The number of student-written tests.
     */
    public int getNumStudentWrittenTests() {
        return getStudentOutcomes().size();
    }
    
    /**
     * Return number of TestOutcomes with given outcome.
     * 
     * @param outcome
     *            the outcome (PASSED or FAILED/ERROR/HUH)
     * @return number of TestOutcomes with given outcome
     */
    public int countOutcomes(String outcome) {
        int count = 0;
        for (TestOutcome testOutcome : testOutcomes) {
            if (testOutcome.getOutcome().equals(outcome))
                ++count;
        }
        return count;
    }
    
    public int countCardinalOutcomes(String outcome) {
        int count = 0;
        for (TestOutcome testOutcome : getAllTestOutcomes()) {
            if (testOutcome.getOutcome().equals(outcome))
                ++count;
        }
        return count;
    }
    
    /**
     * Count the number of outcomes with the given outcome in a given list of testOutcomes
     * @param outcome the outcome to count
     * @param outcomeList the list of testOutcomes
     * @return the count of the number of instances of the given outcome
     */
    int countOutcomes(String outcome, List<TestOutcome> outcomeList)
    {
        int count=0;
        for (Iterator<TestOutcome> i = outcomeList.iterator(); i.hasNext();) {
            TestOutcome testOutcome = i.next();
            if (testOutcome.getOutcome().equals(outcome))
                ++count;
        }
        return count;
    }
    
    private int countNonPassedOutcomes()
    {
        int nonPassed=0;
        for (TestOutcome outcome : getAllTestOutcomes()) {
            if (outcome.isFailed())
                nonPassed++;
        }
        return nonPassed;
    }
    
    private int scoreNonPassedOutcomes()
    {
        int nonPassedScore=0;
        for (Iterator<TestOutcome> ii=testOutcomes.iterator(); ii.hasNext();)
        {
            TestOutcome outcome = ii.next();
            if (outcome.getTestType().equals(TestOutcome.FINDBUGS_TEST))
                continue;
            if (!outcome.getOutcome().equals(TestOutcome.PASSED))
                nonPassedScore += outcome.getPointValue();
        }
        return nonPassedScore;
    }

    private int scoreOutcomes(String outcome) {
        return scoreOutcomes(outcome, testOutcomes);
    }

    private int scoreOutcomes(String outcome, List<TestOutcome> list) {
        int count = 0;
        for (Iterator<TestOutcome> i = list.iterator(); i.hasNext();) {
            TestOutcome testOutcome = i.next();
            if (testOutcome.getTestType().equals(TestOutcome.FINDBUGS_TEST))
                continue;
            if (testOutcome.getOutcome().equals(outcome))
                count += testOutcome.getPointValue();
        }
        return count;
    }
    private int scoreOutcomes(List<TestOutcome> list) {
        int count = 0;
        for (Iterator<TestOutcome> i = list.iterator(); i.hasNext();) {
            TestOutcome testOutcome = i.next();
  
                count += testOutcome.getPointValue();
        }
        return count;
    }
    /**
     * Get a list of the PUBLIC, RELEASE and SECRET tests.  
     * Does <b>not</b> return FindBugs warnings.
     * 
     * @return a List of the build and quick outcomes.
     */
    public List<TestOutcome> getAllTestOutcomes() {
        List<TestOutcome> outcomes = getOutcomesForTestType(TestOutcome.PUBLIC_TEST);
        outcomes.addAll(getOutcomesForTestType(TestOutcome.RELEASE_TEST));
        outcomes.addAll(getOutcomesForTestType(TestOutcome.SECRET_TEST));
        return outcomes;
    }

    /**
     * Gets only the build and quick outcomes
     * 
     * @return a List of the build and quick outcomes.
     */
    public TestOutcome getBuildOutcome() {
        List buildOutcomes = getOutcomesForTestType(TestOutcome.BUILD_TEST);
        return (TestOutcome) buildOutcomes.get(0);
    }

    
    /**
     * Gets only the public outcomes
     * @return a List of the build and quick outcomes.
     */
    public List<TestOutcome> getPublicOutcomes() {
        List<TestOutcome> publicOutcomes = getOutcomesForTestType(TestOutcome.PUBLIC_TEST);
        return publicOutcomes;
    }

    public boolean getCouldNotRunPublicTests() {
        List publicOutcomes = getOutcomesForTestType(TestOutcome.PUBLIC_TEST);
        if (publicOutcomes.size() != 1) return false;
        return ((TestOutcome)publicOutcomes.get(0)).getOutcome().equals(TestOutcome.COULD_NOT_RUN);
    }

    public boolean getCouldNotRunReleaseTests() {
        List releaseOutcomes = getOutcomesForTestType(TestOutcome.RELEASE_TEST);
        if (releaseOutcomes.size() != 1) return false;
        return ((TestOutcome)releaseOutcomes.get(0)).getOutcome().equals(TestOutcome.COULD_NOT_RUN);
    }

    public boolean getCouldNotRunSecretTests() {
        List secretOutcomes = getOutcomesForTestType(TestOutcome.SECRET_TEST);
        if (secretOutcomes.size() != 1) return false;
        return ((TestOutcome)secretOutcomes.get(0)).getOutcome().equals(TestOutcome.COULD_NOT_RUN);
    }

    /**
     * Gets a list of release test outcomes.
     * 
     * @return A list of all release test outcomes contained in this collection.
     */
    public List<TestOutcome> getReleaseOutcomes() {
        return getOutcomesForTestType(TestOutcome.RELEASE_TEST);
    }

    /**
     * Gets a list of the secret test outcomes.
     * 
     * @return A list of the secret test outcomes in this collection.
     */
    public List<TestOutcome> getSecretOutcomes() {
        return getOutcomesForTestType(TestOutcome.SECRET_TEST);
    }
    /**
     * Gets a list of student test outcomes.
     * @return A list of all student test outcomes in this collection.
     */
    public List<TestOutcome> getStudentOutcomes() {
    	return getOutcomesForTestType(TestOutcome.STUDENT_TEST);
    }

    /**
     * Gets only the findbugs outcomes.
     * @return a List of the findbugs outcomes.
     */
    public List<TestOutcome> getFindBugsOutcomes() {
        return getOutcomesForTestType(TestOutcome.FINDBUGS_TEST);
    }
    
    /**
     * Gets only the PMD outcomes.
     * @return a List of the pmd outcomes.
     */
    public List getPmdOutcomes() {
        return getOutcomesForTestType(TestOutcome.PMD_TEST);
    }

    /**
     * Checks if the submission that produced this collection of test outcomes
     * is release eligible. A submission is release eligible if all of the build
     * and quick test have passed.
     * 
     * @return true if all of the build and quick tests passed; false if any of
     *         them failed.
     */
    public boolean isReleaseEligible() {
        if (!isCompileSuccessful())
            return false;
        for (Iterator<TestOutcome> it = getPublicOutcomes().iterator(); it.hasNext();) {
            TestOutcome outcome = it.next();

            if (!outcome.getStudentOutcome().equals(TestOutcome.PASSED))
                return false;
        }
        return true;
    }

    /**
     * Constructor.
     */
    public TestOutcomeCollection() {
    }

    /**
     * Add a TestOutcome.
     * 
     * @param outcome
     *            the TestOutcome to add
     */
    public void add(TestOutcome outcome) {
        if (testRunPK==null)
            testRunPK=outcome.getTestRunPK();
        if (outcome.getTestType().equals(TestOutcome.BUILD_TEST))
            compileSuccessful = outcome.getOutcome().equals(TestOutcome.PASSED);
        testOutcomes.add(outcome);
        testMap.put(outcome.getTestName(), outcome);
    }
    
    public TestOutcome getTest(String name) {
        return testMap.get(name);
    }
    
    public Map<String,TestOutcome> getTestMap() {
        return testMap;
    }

    public void addAll(Collection collection) {
        for(Iterator i = collection.iterator(); i.hasNext(); ) {
            TestOutcome outcome = (TestOutcome) i.next();
            add(outcome);
        }
    }

    private static final String[] cardinalTestTypesArr = new String[] {TestOutcome.PUBLIC_TEST, 
		TestOutcome.RELEASE_TEST,
		TestOutcome.SECRET_TEST};
    public Iterator<TestOutcome> cardinalTestTypesIterator()
    {
    	return iterator(cardinalTestTypesArr);
    }
    
    public Iterator<TestOutcome> findBugsIterator() {
    	return iterator(TestOutcome.FINDBUGS_TEST);
    }
    
    public Iterable<TestOutcome> getIterableForCardinalTestTypes()
    {
    	return new Iterable<TestOutcome>() {

			/* (non-Javadoc)
			 * @see java.lang.Iterable#iterator()
			 */
			public Iterator<TestOutcome> iterator()
			{
				return cardinalTestTypesIterator();
			}
    		
    	};
    }
    
    private Iterator<TestOutcome> iterator(final String[] testTypes) {
        return new Iterator<TestOutcome>() {
            private Iterator<TestOutcome> ii = testOutcomes.iterator();

            private TestOutcome next;

            public boolean hasNext() {
                while (ii.hasNext()) {
                    TestOutcome testOutcome = ii.next();
                    for (int jj = 0; jj < testTypes.length; jj++) {
                        if (testOutcome.getTestType().equals(testTypes[jj])) {
                            next = testOutcome;
                            return true;
                        }
                    }
                }
                return false;
            }

            public TestOutcome next() {
                if (next == null)
                    throw new NoSuchElementException(
                            "No elements remaining in this iterator");
                return next;
            }

            public void remove() {
                throw new UnsupportedOperationException("cannot call remove here");
            }
        };
    }

    private Iterator<TestOutcome> iterator(final String testType) {
        return iterator(new String[] { testType });
    }

    /**
     * Returns a list of the outcomes mapped to the given test type.
     * 
     * @param testType the type of test outcomes we want
     * @return a List of the test outcomes of the given type that are contained
     *         in this collection.
     */
    private List<TestOutcome> getOutcomesForTestType(String testType) {
        List<TestOutcome> result = new ArrayList<TestOutcome>();
        for (Iterator ii = iterator(testType); ii.hasNext();) {
            result.add((TestOutcome)ii.next());
        }
        return result;
    }

    /**
     * Return a Collection of all the TestOutcomes.
     * 
     * @return a Collection of all the TestOutcomes
     */
    public List<TestOutcome> getAllOutcomes() {
        return testOutcomes;
    }

    /**
     * Get an Iterator over all the TestOutcomes.
     * 
     * @return Iterator over all the TestOutcomes
     */
    public Iterator<TestOutcome> iterator() {
        return testOutcomes.iterator();
    }

    public void dump(PrintStream out) {
        for (Iterator<TestOutcome> i = iterator(); i.hasNext();) {
            TestOutcome outcome = i.next();
            out.println(outcome.toString());
        }
    }

    /**
     * Returns the number of test outcomes contained in this collection.
     * 
     * @return The number of test outcomes contained in this collection.
     */
    public int size() {
        return testOutcomes.size();
    }

    /**
     * Read collection from given ObjectInputSTream source. TestOutcomes are added
     * until EOF is reached.
     * 
     * @param in the ObjectInputStream source
     */
    public void read(ObjectInputStream in) throws IOException {
        try {
            List outcomes = (List) in.readObject();
            for (Iterator i = outcomes.iterator(); i.hasNext();)
                add((TestOutcome) i.next());

        } catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Write collection to given ObjectOutputStream sink.
     * 
     * @param out
     *            the ObjectOutputStream sink
     * @throws IOException
     */
    public void write(ObjectOutputStream out) throws IOException {
            out.writeObject(testOutcomes);
    }

    List<String> getHeaders() {
        List<String> headers = new ArrayList<String>();
        for (Iterator ii = getOutcomesForTestType(TestOutcome.BUILD_TEST).iterator(); ii
                .hasNext();) {
            TestOutcome outcome = (TestOutcome) ii.next();

            headers.add("b" + Formats.twoDigitInt.format(outcome.getTestNumber()));
        }
        for (Iterator ii = getOutcomesForTestType(TestOutcome.PUBLIC_TEST).iterator(); ii
                .hasNext();) {
            TestOutcome outcome = (TestOutcome) ii.next();

            headers.add("q" + Formats.twoDigitInt.format(outcome.getTestNumber()));
        }
        for (Iterator ii = getOutcomesForTestType(TestOutcome.RELEASE_TEST).iterator(); ii
                .hasNext();) {
            TestOutcome outcome = (TestOutcome) ii.next();

            headers.add("r" + Formats.twoDigitInt.format(outcome.getTestNumber()));
        }
        return headers;
    }

    /* (non-Javadoc)
     * @see edu.umd.cs.marmoset.modelClasses.ITestSummary#getNumFindBugsWarnings()
     */
    public int getNumFindBugsWarnings() {
        return countOutcomes(TestOutcome.WARNING);
    }


   

    /**
     * Private helper method that executes the prepared statement and returns a
     * collection of test outcomes.
     * 
     * @param stmt
     *            the statement to execute
     * @return a collection of the test outcomes returned by the given prepared
     *         statement. The collection will never be null, though it might be
     *         empty.
     * @throws SQLException
     */
    private static TestOutcomeCollection getAllFromPreparedStatement(
            PreparedStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        
        TestOutcomeCollection testOutcomes = new TestOutcomeCollection();
        
        while (rs.next()) {
            TestOutcome outcome = new TestOutcome();
            outcome.fetchValues(rs, 1);
            testOutcomes.add(outcome);
        }
        return testOutcomes;
    }

    /**
     * @param testRunPK
     * @param conn
     * @return
     */
    public static TestOutcomeCollection lookupByTestRunPK(String testRunPK,
            Connection conn) throws SQLException {
        String query = " SELECT " + TestOutcome.ATTRIBUTES + " FROM test_outcomes "
                + " WHERE test_run_pk = ? ";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, testRunPK);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    public static TestOutcomeCollection lookupBySubmissionPK(
            String submissionPK,
            Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestOutcome.ATTRIBUTES+
            " FROM test_outcomes, submissions " +
            " WHERE submissions.current_test_run_pk = test_outcomes.test_run_pk" +
            " AND submissions.submission_pk = ? ";
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, submissionPK);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * Sets the testRunPKs to the given value. This method is used by the
     * SubmitServer to set the testRunPK of all the testOutcomes in a collection
     * returned by the BuildServer. The BuildServer leaves this field empty
     * because it is unknown until the SubmitServer inserts a new testRun
     * object, therefore generating the testRunPK.
     * 
     * @param testRunPK
     *            the testRunPK to set
     */
    public void updateTestRunPK(String testRunPK) {
        for (Iterator<TestOutcome> ii = iterator(); ii.hasNext();) {
            TestOutcome testOutcome = ii.next();
            testOutcome.setTestRunPK(testRunPK);
        }
    }

    /**
     * Insert the contents of a TestOutcomeCollection into the test_outcomes
     * table.
     * 
     * TODO consider making this an instance method of TestOutcomeCollection
     * 
     * @param conn
     *            the database connection
     * @param testOutcomeCollection
     *            the collection of test outcomes to be inserted
     * @throws SQLException
     */
    public void insert(Connection conn) throws SQLException {
        String query = " INSERT INTO " + TestOutcome.TABLE_NAME + 
        " SET "+
        " test_run_pk = ?, " +
        " test_type = ?," +
        " test_number = ?, " +
        " outcome = ?, " +
        " point_value = ?, " +
        " test_name = ?, " +
        " short_test_result = ?, " +
        " long_test_result = ?," +
        " exception_class_name = ?, " +
        " coarsest_coverage_level = ?," +
        " exception_source_covered_elsewhere = ?, " +
        " details = ? ";

        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(query);

            // Put values into statement (preparing for bulk insert)
            for (TestOutcome outcome : testOutcomes) {
                outcome.putValues(stmt, 1);
                stmt.addBatch();
            }

            // Insert the values!
            stmt.executeBatch();

        } finally {
            Queries.closeStatement(stmt);
        }
    }
    
    /**
     * Performs a batch update of the point values of each test outcome in this collection
     * after an AssignPoints operation.
     * For efficiency, this batch update only affects the point values and does not try
     * to update any other fields.
     * @param conn
     * @throws SQLException
     */
    public void batchUpdatePointValues(Connection conn)
    throws SQLException
    {
        String update =
            " UPDATE " + TestOutcome.TABLE_NAME +
            " SET " +
            " point_value = ? " +
            " WHERE test_run_pk = ? " +
            " AND test_type = ? " +
            " AND test_number = ? ";
        
        PreparedStatement stmt=null;
        try {
            stmt = conn.prepareStatement(update);

            for (TestOutcome outcome : testOutcomes) {
                int index=1;
                stmt.setInt(index++, outcome.getPointValue());
                stmt.setString(index++, outcome.getTestRunPK());
                stmt.setString(index++, outcome.getTestType());
                stmt.setInt(index++, outcome.getTestNumber());
                stmt.addBatch();
            }

            stmt.executeBatch();
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg0) {
        TestOutcomeCollection that = (TestOutcomeCollection) arg0;
        return getValuePassedOverall() - that.getValuePassedOverall();
    }

    /**
     * Gets all of the test outcomes from the canonical run for a given project.
     * @param projectPK the projectPK
     * @param conn the connection to the database
     * @return a collection of test outcomes from the canonical run for a given project;
     * returns an empty collection if there is no valid canonical run for the project.
     */
    public static TestOutcomeCollection lookupCanonicalOutcomesByProjectPK(String projectPK, Connection conn)
    throws SQLException
    {
        String query = 
            " SELECT " +TestOutcome.ATTRIBUTES+
            " FROM test_outcomes, projects, project_jarfiles " +
            " WHERE projects.project_pk = ? " +
            " AND projects.project_jarfile_pk = project_jarfiles.project_jarfile_pk " +
            " AND project_jarfiles.test_run_pk = test_outcomes.test_run_pk ";
        
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, projectPK);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    /**
     * Returns the number of outcomes for each test type in the given array
     * @param outcomeArr
     * @return number of outcomes for each test type in the given array
     */
    private int countOutcomes(String[] outcomeArr)
    {
        int count=0;
        for (int ii=0; ii < outcomeArr.length; ii++)
        {
            count += countOutcomes(outcomeArr[ii]);
        }
        return count;
    }
    
    /**
     * Get the number of tests that failed due to a fault (HUH, ERROR, FAILED) rather than
     * being unimplemented.
     * @return the number of tests that failed due to a fault (HUH, ERROR, FAILED).
     */
    public int countFaults()
    {
        return countOutcomes(new String[] {
                TestOutcome.HUH,
                TestOutcome.ERROR,
                TestOutcome.FAILED,
                TestOutcome.TIMEOUT
        });
    }
    
    private CodeCoverageResults getCodeCoverageResultsOfGivenType(String[] types)
    throws IOException
    {
    	CodeCoverageResults results = new CodeCoverageResults();
    	Set<String> set = new HashSet<String>();
    	for (String type : types) {
    		set.add(type);
    	}
    	for (TestOutcome outcome : testOutcomes) {
    		// Note that timeouts don't produce any coverage output 
            // but still shouldn't cause everything else to fail!
            if (set.contains(outcome.getTestType()) && outcome.isCoverageType() && !outcome.getOutcome().equals(TestOutcome.TIMEOUT)) {
                //System.out.println("Adding test of type " +outcome.getTestType());
    			CodeCoverageResults currentCoverageResults = outcome.getCodeCoverageResults();
    			results.union(currentCoverageResults);
    		}
    	}
    	return results;
    }

    /**
     * Get the overall code coverage results for the public, release and secret tests.
     * @return CodeCoverageResults object representing the aggregate results of code coverage.
     * @throws IOException
     */
    public CodeCoverageResults getOverallCoverageResultsForCardinalTests()
    throws IOException
    {
    	return getCodeCoverageResultsOfGivenType(new String[] {
    			TestOutcome.PUBLIC_TEST, 
    			TestOutcome.RELEASE_TEST,
    			TestOutcome.SECRET_TEST});
    }
    
    public CodeCoverageResults getOverallCoverageResultsForPublicTests()
    throws IOException
    {
    	return getCodeCoverageResultsOfGivenType(new String[] {TestOutcome.PUBLIC_TEST});
    }
    
    public CodeCoverageResults getOverallCoverageResultsForReleaseTests()
    throws IOException
    {
    	return getCodeCoverageResultsOfGivenType(new String[] {TestOutcome.RELEASE_TEST});
    }
    
    public CodeCoverageResults getOverallCoverageResultsForSecretTests()
    throws IOException
    {
    	return getCodeCoverageResultsOfGivenType(new String[] {TestOutcome.SECRET_TEST});
    }
    
    public CodeCoverageResults getOverallCoverageResultsForStudentTests()
    throws IOException
    {
    	return getCodeCoverageResultsOfGivenType(new String[] {TestOutcome.STUDENT_TEST});
    }
    
    public CodeCoverageResults getOverallCoverageResultsForPublicAndStudentTests()
    throws IOException
    {
    	return getCodeCoverageResultsOfGivenType(new String[] {TestOutcome.PUBLIC_TEST, TestOutcome.STUDENT_TEST});
    }
    /**
     * Get the code coverage results divided up by packages.
     * @param packageNameList
     * @return A map of the package names to maps of the different "interesting" categories
     * 		mapped to their corresponding coverage results.
     * @throws IOException
     */
    public Map<String,Map<String,CodeCoverageResults>> getCoverageResultsByPackageMap(
    	Iterable<String> packageNameList)
    throws IOException
    {
    	Map<String,Map<String,CodeCoverageResults>> resultMap= new HashMap<String,Map<String,CodeCoverageResults>>();
    	// map from package-names to a map of "interesting" coverage results categories
    	// (i.e. public, student, public + student, etc) to their corresponding results
    	Map<String,CodeCoverageResults> coverageMap = getCoverageResultsMap();
    	
    	for (String packageName : packageNameList) {
    		// This map will map code coverage category (public, student, etc) to the results
    		// filtered for the package name
    		Map<String,CodeCoverageResults> mapForPackageName = new HashMap<String,CodeCoverageResults>();
    		for (Entry<String,CodeCoverageResults> entry : coverageMap.entrySet()) {
    			CodeCoverageResults results = entry.getValue();
    			CodeCoverageResults resultsForPackage = results.getCodeCoverageResultsForPackage(packageName);
    			mapForPackageName.put(entry.getKey(), resultsForPackage);
    		}
    		resultMap.put(packageName, mapForPackageName);
    	}
    	return resultMap;
    }
    
    public List<TestOutcome> getUncoveredMethods()
    {
        return getOutcomesForTestType(TestOutcome.UNCOVERED_METHOD);
    }
    
    /**
     * Gets a map of the different "interesting" categories of coverage (public, student,
     * public + student, cardinal, public + student intersect cardinal) to their corresponding
     * coverage results.
     * TODO return null if coverage information is not available?
     * @return 
     * @throws IOException
     */
    public Map<String,CodeCoverageResults> getCoverageResultsMap()
	throws IOException
	{
		Map<String,CodeCoverageResults> codeCoverageResultsMap = new HashMap<String,CodeCoverageResults>();
		
		CodeCoverageResults publicCoverageResults = getOverallCoverageResultsForPublicTests();
		//CodeCoverageResults releaseCoverageResults = getOverallCoverageResultsForReleaseTests();
		CodeCoverageResults studentCoverageResults = getOverallCoverageResultsForStudentTests();
		CodeCoverageResults cardinalCoverageResults = getOverallCoverageResultsForCardinalTests();
						
		CodeCoverageResults publicAndStudentCoverageResults = new CodeCoverageResults(publicCoverageResults);
		publicAndStudentCoverageResults.union(studentCoverageResults);
		
		CodeCoverageResults intersectionCoverageResults = new CodeCoverageResults(studentCoverageResults);
		intersectionCoverageResults.union(publicAndStudentCoverageResults);
		intersectionCoverageResults.intersect(cardinalCoverageResults);
		
		codeCoverageResultsMap.put("public",publicCoverageResults);
		codeCoverageResultsMap.put("student", studentCoverageResults);
		codeCoverageResultsMap.put("cardinal",cardinalCoverageResults);
		codeCoverageResultsMap.put("public_and_student",publicAndStudentCoverageResults);
		codeCoverageResultsMap.put("public_and_student_intersect_cardinal",intersectionCoverageResults);
		return codeCoverageResultsMap;
	}
    
    public List<TestOutcome> getTestOutcomesWithStackTraceAtLine(FileNameLineNumberPair pair)
    {
        return getTestOutcomesWithStackTraceAtLine(pair.getFileName(), pair.getLineNumber());
    }
    
    List<TestOutcome> getTestOutcomesWithStackTraceAtLine(String filename, int lineNumber)
    {
        List<TestOutcome> result = new LinkedList<TestOutcome>();
        for (Iterator<TestOutcome> ii=iterator(); ii.hasNext();) {
            TestOutcome outcome = ii.next();
            if (outcome.isStackTraceAtLineForFile(filename, lineNumber))
                result.add(outcome);
        }
        return result;
    }

    /**
     * Returns a collection of the testOutcomes that cover the given file at 
     * the given line number.  This method (obviously) only returns public, release, 
     * secret, and student-written tests.
     * 
     * @param filename the name of the file
     * @param lineNumber the number of the line of the file
     * @return a testOutcomeCollection of the testOutcomes that cover the given source line of
     * the given source file.
     */
    public TestOutcomeCollection getTestOutcomesCoveringFileAtLine(String filename, int lineNumber)
    throws IOException
    {
    	TestOutcomeCollection results = new TestOutcomeCollection();
    	for (TestOutcome outcome : testOutcomes) {
    		if (outcome.isCoverageType() && outcome.coversFileAtLineNumber(filename, lineNumber)) {
    			results.add(outcome);
    		}
    	}
    	return results;
    }
    
    /**
     * Gets a list of cardinal tests (public, release or secret) that fail due to
     * the given (runtime) exception.
     * @param exceptionClassName The fully-qualified classname of the exception.
     * @return A List&lt;TestOutcome&gt; of test outcomes that fail due to the
     * given runtime exception.
     */
    public List<TestOutcome> getFailingCardinalOutcomesDueToException(@NonNull String exceptionClassName)
    {
        List<TestOutcome> list=new LinkedList<TestOutcome>();
        for (TestOutcome outcome : getAllTestOutcomes()) {
            if (outcome.isError() && exceptionClassName.equals(outcome.getExceptionClassName())) {
                list.add(outcome);
            }
        }
        return list;
    }

    /**
     * Gets the testOutcomes that cover the given findbugs warning.
     * @param findbugsWarning The findbugs warning
     * @return The collection of testOutcomes that cover the given FindBugs warning.
     * @throws DocumentException
     * @throws IOException
     */
    public TestOutcomeCollection getTestOutcomesCoveredByFindbugsWarning(
    		TestOutcome findbugsWarning)
    throws IOException
    {
        if (!findbugsWarning.isFindBugsWarning())
            throw new IllegalArgumentException("This method requires a FindBugs test outcome");
        
        FileNameLineNumberPair pair = findbugsWarning.getFileNameLineNumberPair();
        // If the given FindBugs warning doesn't contain any line numbers; return an empty collection
        if (pair == null || pair.getLineNumber() == -1)
            return new TestOutcomeCollection();
        return getTestOutcomesCoveringFileAtLine(pair.getFileName(), pair.getLineNumber());
    }

	public TestOutcome getOutcomeByTestTypeAndTestNumber(String testType, String testNumber)
	{
		int testNumberInt = Integer.parseInt(testNumber);
		for (TestOutcome outcome : testOutcomes) {
			if (outcome.getTestType().equals(testType) && outcome.getTestNumber() == testNumberInt)
				return outcome;
		}
		return null;
	}
    
    public static class TarantulaScore
    {
        public final double score;
        public final double intensity;
        private StringBuffer buf;
        public TarantulaScore(double score, double intensity, StringBuffer buf) {
            this.score=score;
            this.intensity=intensity;
            this.buf=buf;
        }
    }
    
    public TarantulaScore getTarantulaScoreForFileAtLine(String filename, int lineNo)
    throws IOException
    {
//      FIXME: Should handle any selected test type, not only all cardinal test types
        StringBuffer buf=new StringBuffer();
        double score=-1.0;
        double intensity=-1.0;
        Iterable<TestOutcome> iterable= getIterableForCardinalTestTypes();
        
        boolean isLineExecutable=false;
        
        int totalTests=0;
        int passed=0;
        int totalPassed=0;
        int failed=0;
        int totalFailed=0;
        int countByOutcome=0;
        for (TestOutcome outcome : iterable) {
            totalTests++;
            FileWithCoverage coverageForGivenOutcome = outcome.getCodeCoverageResults().getFileWithCoverage(filename);

            countByOutcome = coverageForGivenOutcome.getStmtCoverageCount(lineNo);
            if (outcome.getOutcome().equals(TestOutcome.PASSED))
                totalPassed++;
            else
                totalFailed++;
            
            if (countByOutcome > 0) {
                isLineExecutable=true;
                if (outcome.getOutcome().equals(TestOutcome.PASSED)) {
                    // failed outcome
                    buf.append("<td class=\"passed\">"+countByOutcome+"</td>\n");
                    passed++;
                } else {
                    buf.append("<td class=\"failed\">"+countByOutcome+"</td>\n");
                    failed++;
                }
            } else if (countByOutcome < 0){
                buf.append("<td></td>\n");
            } else {
                buf.append("<td></td>\n");
            }
        }
        if (isLineExecutable) {
            double passedPct = totalPassed>0?(double)passed / (double)totalPassed:0.0;
            double failedPct = totalFailed>0?(double)failed / (double)totalFailed:0.0;

            intensity = Math.max(passedPct, failedPct);
            score=passedPct/(passedPct+failedPct);
            
//            System.out.println(fileWithCoverage.getShortFileName() +
//                ", lineNumber = "+lineNo+
//                ", executable? "+isLineExecutable+
//                ", countByOutcome = "+countByOutcome+
//                ", totalPassed = "+totalPassed+
//                ", totalFailed = "+totalFailed+
//                ", passed = " +passed+
//                ", failed = " +failed+
//                ", passedPct = "+passedPct+
//                ", failedPct = "+failedPct+
//                ", score = "+score);
        }
        return new TarantulaScore(score, intensity, buf);
    }
    
    /**
     * Look up a list of all of the the TestOutcomeCollections with a given submissionPK and
     * projectJarfilePK.  The additional TestOutcomeCollections will be due to explicit retests
     * or background retests.
     * TODO invalidate test runs.  
     * @param submissionPK
     * @param projectJarfilePK
     * @param conn
     * @return A list of all the TestOutcomeCollections for the given submissionPK 
     *  and projectJarfilePK.
     * @throws SQLException
     */
    public static List<TestOutcomeCollection> lookupAllBySubmissionPKAndProjectJarfilePK(
        String submissionPK,
        String projectJarfilePK,
        Connection conn)
    throws SQLException
    {
        String query=
            " SELECT " +TestOutcome.ATTRIBUTES+ " " +
            " FROM test_outcomes, test_runs " +
            " WHERE submission_pk = ? " +
            " AND test_runs.project_jarfile_pk = ? " +
            " AND test_runs.test_run_pk = test_outcomes.test_run_pk " +
            " ORDER BY test_outcomes.test_run_pk ";
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1,submissionPK);
            stmt.setString(2,projectJarfilePK);
            ResultSet rs=stmt.executeQuery();
            // Could also use a map to store results
            List<TestOutcomeCollection> list=new LinkedList<TestOutcomeCollection>();
            TestOutcome previous=null;
            TestOutcomeCollection currentCollection=new TestOutcomeCollection();
            while (rs.next()) {
                TestOutcome outcome=new TestOutcome();
                outcome.fetchValues(rs,1);
                // If these two outcomes have different testRunPK, then they represent
                // the boundary between different testOutcomeCollections.
                // So add the currentCollection the list of results, and start the next collection.
                if (previous!=null && !previous.getTestRunPK().equals(outcome.getTestRunPK())) {
                    list.add(currentCollection);
                    currentCollection=new TestOutcomeCollection();
                }
                previous=outcome;
                currentCollection.add(outcome);
            }
            // Add outcomes in the last collection, if any.
            if (currentCollection.size() > 0)
                list.add(currentCollection);
            return list;
        } finally {
            Queries.closeStatement(stmt);
        }
    }

    public CodeCoverageResults getOverallCoverageResultsForAllPassingTests()
    {
        CodeCoverageResults results=new CodeCoverageResults();
        Set<String> set=new HashSet<String>();
        set.add(TestOutcome.PUBLIC_TEST);
        set.add(TestOutcome.RELEASE_TEST);
        set.add(TestOutcome.SECRET_TEST);
        set.add(TestOutcome.STUDENT_TEST);
        for (TestOutcome outcome : testOutcomes) {
            try {
                if (set.contains(outcome.getTestType()) && outcome.getOutcome().equals(TestOutcome.PASSED)) {
                    results.union(outcome.getCodeCoverageResults());
                }
            } catch (IOException e) {
                // Ignore and keep trying
                // TODO Log this someplace with a logger
            }
        }
        return results;
    }

    public CodeCoverageResults getOverallCoverageResultsForReleaseUnique()
    throws IOException
    {
        CodeCoverageResults codeCoverageResults=getOverallCoverageResultsForReleaseTests();
        
        CodeCoverageResults nonReleaseResults=getOverallCoverageResultsForPublicAndStudentTests();
        nonReleaseResults.union(getOverallCoverageResultsForSecretTests());
        codeCoverageResults.excluding(nonReleaseResults);
        return codeCoverageResults;
    }
    
    public static boolean isApproximatelyCovered(TestOutcomeCollection collection,
        TestOutcome outcome)
    throws IOException
    {
        return collection.isExceptionSourceApproximatelyCovered(outcome, 3);
    }
    
    /**
     * Check if the coverage of a given release test that failed due to a run-time 
     * exception has public/student tests that cover the source of the exception
     * or range lines before it
     * @param releaseTest
     * @param range the number of lines before the exception source that needs to be covered
     *  to constitute "approximate" coverage
     * @return True if the public/student tests approximately cover the source of the exception;
     *  false otherwise
     * @throws IOException
     */
    boolean isExceptionSourceApproximatelyCovered(TestOutcome releaseTest, int range)
    throws IOException
    {
        // Doesn't make sense to mix test outcomes from different test runs
        assert(releaseTest.getTestRunPK().equals(testRunPK));
        return releaseTest.isExceptionSourceApproximatelyCoveredElsewhere(getOverallCoverageResultsForPublicAndStudentTests(), range);
    }

    public List<TestOutcome> getReleaseAndSecretOutcomes()
    {
        List<TestOutcome> releaseList=getReleaseOutcomes();
        List<TestOutcome> secretList=getSecretOutcomes();
        releaseList.addAll(secretList);
        return releaseList;
    }

    /**
     * Returns a list of all FindBugs outcomes (warnings) in this collection
     * that start with the given bug-code prefix.
     * @param warningPrefix Prefix of bug-code warning that result list of 
     *  bug-codes must begin with.
     * @return List of all FindBugs outcomes (warnings) contained in this collection
     *  that start with the given bug-code prefix.
     */
    public List<TestOutcome> getFindBugsOutcomesWithWarningPrefix(String warningPrefix)
    {
        List<TestOutcome> list=new LinkedList<TestOutcome>();
        for (TestOutcome outcome : getFindBugsOutcomes()) {
            if (outcome.getTestName().startsWith(warningPrefix)) {
                list.add(outcome);
            }
        }
        return list;
    }
}
