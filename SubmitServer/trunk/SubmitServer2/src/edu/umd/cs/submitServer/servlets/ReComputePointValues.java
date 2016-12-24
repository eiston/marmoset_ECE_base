/*
 * Copyright (C) 2005, University of Maryland
 * All Rights Reserved
 * Created on Feb 9, 2005
 *
 */
package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.ITestSummary;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.modelClasses.TestOutcomeCollection;
import edu.umd.cs.marmoset.modelClasses.TestRun;

/**
 * @author jspacco
 *
 */
public class ReComputePointValues extends SubmitServerServlet
{

    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        Connection conn=null;
        boolean transactionSuccess=false;
        try {
            conn=getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            
            Project project = (Project)request.getAttribute("project");
            
            // find all the test runs for this project
            List<TestRun> testRunList = TestRun.lookupAllByProjectPK(
                    project.getProjectPK(),
                    conn);
            
            response.setContentType("text/plain");
            PrintWriter out = response.getWriter();
            
            out.println("we've found " +testRunList.size()+ " test runs");
            
            for (Iterator ii=testRunList.iterator(); ii.hasNext();)
            {
                TestRun testRun = (TestRun)ii.next();
                
                out.println("testRunPK: " +testRun.getTestRunPK());

                // fetch the corresponding testOutcomeCollection
                ITestSummary outcomes = TestOutcomeCollection.lookupByTestRunPK(
                        testRun.getTestRunPK(),
                        conn);

                // update the testRun's information
                testRun.setCompileSuccessful(outcomes.isCompileSuccessful());
                testRun.setValuePublicTestsPassed(outcomes.getValuePublicTestsPassed());
                testRun.setValueReleaseTestsPassed(outcomes.getValueReleaseTestsPassed());
                testRun.setValueSecretTestsPassed(outcomes.getValueSecretTestsPassed());
                testRun.setValuePassedOverall(outcomes.getValuePassedOverall());
                testRun.update(conn);
                
                out.println("outcomes.getValuePassedOverall: " +outcomes.getValuePassedOverall());
                
                // if it exists, get the submission that has this testRun as its currentTestRun
                Submission submission = Submission.lookupByTestRunPK(
                        testRun.getTestRunPK(),
                        conn);

                // set the cached results for the current test run of this submission 
                if (submission != null)
                {
                    out.println("recomputing for submissionPK: " +submission.getSubmissionPK()
                            + ", getValuePassedOverall: " +outcomes.getValuePassedOverall());
                    submission.setCompileSuccessful(outcomes.isCompileSuccessful());
                    submission.setValuePublicTestsPassed(outcomes.getValuePublicTestsPassed());
                    submission.setValueReleaseTestsPassed(outcomes.getValueReleaseTestsPassed());
                    submission.setValueSecretTestsPassed(outcomes.getValueSecretTestsPassed());
                    submission.setValuePassedOverall(outcomes.getValuePassedOverall());
                    submission.update(conn);
                }
            }
            
            conn.commit();
            transactionSuccess=true;
            
            out.flush();
            out.close();
        } catch (SQLException e) {
            handleSQLException(e);
            throw new ServletException(e);
        } finally {
            rollbackIfUnsuccessfulAndAlwaysReleaseConnection(transactionSuccess, conn);
        }
    }
}
