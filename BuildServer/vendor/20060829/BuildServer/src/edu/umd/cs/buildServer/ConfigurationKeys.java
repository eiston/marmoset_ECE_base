/*
 * Copyright (C) 2004, University of Maryland
 * All Rights Reserved
 * Created on Aug 30, 2004
 */
package edu.umd.cs.buildServer;

/**
 * Keys to get values out of a Properties object storing
 * the configuration for a build server.
 * 
 * @author David Hovemeyer
 * @author jspacco
 */
/**
 * @author jspacco
 *
 */
public interface ConfigurationKeys {
	/**
	 * Submit server HTTP protocol. Should expand to
	 * "http" or "https".  Probably "https".
	 */
	public static final String SUBMIT_SERVER_PROTOCOL = "submit.server.protocol";
	
	/** Submit server host. */
    public static final String SUBMIT_SERVER_HOST = "submit.server.host";
    
    /** Submit server port. */
    public static final String SUBMIT_SERVER_PORT = "submit.server.port";
    
    /** Submit server password. */
    public static final String SUBMIT_SERVER_PASSWORD = "submit.server.password";
    
    /** URL path to the RequestProject servlet on the submit server. */
    public static final String SUBMIT_SERVER_REQUESTPROJECT_PATH =
    	"submit.server.requestproject.path";
    
    /** URL path to the GetProjectJar servlet on the submit server. */
    public static final String SUBMIT_SERVER_GETPROJECTJAR_PATH =
    	"submit.server.getprojectjar.path";
    
    /** URL path to the ReportTestResults servlet on the submit server. */
    public static final String SUBMIT_SERVER_REPORTTESTRESULTS_PATH =
    	"submit.server.reporttestresults.path";
    
    /** URL path to the HandleBuildServerLogMessage servlet on the submit server. */      
    public static final String SUBMIT_SERVER_HANDLEBUILDSERVERLOGMESSAGE_PATH =
        "submit.server.handlebuildserverlogmessage.path";
    
    /**
     * List of PKs of courses we support testing for.  Kind of a hack
     * need to think of a more general way to do this.
     */
    public static final String SUPPORTED_COURSE_LIST =
    	"supported.courses";
    
    /**
     * The semester this buildServer can handle courses for.
     */
    public static final String SEMESTER = "semester";
	
	/**
	 * List of regexes specifying source files which should never
	 * be built.  This should probably not be used in a
	 * production environment.
	 */
	public static final String EXCLUDED_SOURCE_FILE_LIST = "build.sourcefiles.excluded";
	
	/**
	 * Optional boolean property: if true, run student JUnit tests.
	 */
	public static final String RUN_STUDENT_TESTS = "run.student.tests";
	
	/**
	 * Prefix of property name defining submission inspection steps to run.
	 * The lowercased version of the project language should be appended
	 * to get the actual property name.
	 */
	public static final String INSPECTION_TOOLS_PFX = "tools.";
	
	/**
	 * Root of instrumented source files to be used in place of "src" for compiling.
	 */
	public static final String INSTRUMENTED_SRC_DIR = "inst-src";
    
    /**
     * Hostname of build server.  Maybe there's some way to
     * get this in Java, but it's easier to specify it in the
     * configuration properties file.
     */
    public static final String HOSTNAME = "hostname";
    
    /**
     * Local build directory in the filesystem, where we'll
     * build and run projects.
     */
    public static final String BUILD_DIRECTORY = "build.directory";
    
    /**
     * BuildServer's home directory.
     */
    public static final String BUILDSERVER_ROOT = "build.server.root";
    
    /**
     * Test files directory.
     */
    public static final String TEST_FILES_DIRECTORY = "test.files.directory";

    
    /**
     * PMD home directory.
     */
    public static final String PMD_HOME = "pmd.home";
    
    /**
     * Location of clover-clean.xml build file for executing the &lt;clover-clean&gt; task.
     */
    public static final String CLOVER_BUILD_FILE = "/tmp/clover-clean.xml";
    
    /**
     * Directory where we put the test jar file.
     * This can't go in either the build directory
     * or the testfiles directory, because it would be
     * visible (and perhaps modifiable) to student code.
     * 
     * <p> Now that I think about it, this directory is
     * perfect for the eventual test jar cache.  Cool.
     */
    public static final String JAR_CACHE_DIRECTORY = "jar.cache.directory";
    
	/**
	 * Directory where submission zipfiles are stored.
	 * This is not needed in normal operation, but can be
	 * useful when collecting data in batch mode.  
	 */
	public static final String SUBMISSION_DIRECTORY = "submission.directory";
	
	/**
	 * Directory in which to store FindBugs results files.
	 * Only used when collecting data in batch mode.
	 */
	public static final String FINDBUGS_OUTPUT_DIRECTORY = "findbugs.output.directory";

	/**
	 * Skip executing tests.
	 * This is obviously not used in production,
	 * but can be useful if you just want results from submission
	 * inspection steps (e.g., FindBugs).
	 */
	public static final String SKIP_TESTS = "skip.tests";
	
	/**
	 * Skip getting build info.
	 * Not for production.
	 */
	public static final String SKIP_BUILD_INFO = "skip.buildinfo";
	
    /**
     * Log directory.
     */
    public static final String LOG_DIRECTORY = "log.directory";
    
    
    
    /** Prefix used on all debug configuration properties. */
    public static final String DEBUG_PFX = "debug.";
    
    /** Debug property: log verbose messages at DEBUG level. */
    public static final String DEBUG_VERBOSE = DEBUG_PFX + "verbose";
    
    /** Debug property: log extremely, extremely verbose messages at the TRACE level. */
    public static final String DEBUG_TRACE = DEBUG_PFX + "trace";
    
    /** Debug property: don't loop (i.e., only do one request). */
    public static final String DEBUG_DO_NOT_LOOP = DEBUG_PFX + "donotloop";

    /** Turn on java security debug output. */
    public static final String DEBUG_SECURITY = DEBUG_PFX + "security";
    
    /** Alternate property to turn on Java security debug output. */
    public static final String DEBUG_SECURITY_ALT = DEBUG_PFX + "java.security";
    
    /** Don't delete submission zipfiles. */
    public static final String DEBUG_PRESERVE_SUBMISSION_ZIPFILES = DEBUG_PFX + "preservesubmissions";

    /**
     * Key in the test properties config used to lookup the path to the clover database.
     * 
     * <b>NOTE:</b> The actual path, created at initConfig() time, is set to
     * /tmp/myclover.db.[random hexadecimal string].  This allows multiple buildservers
     * to operate on the same host.
     * <p>
     * <b> TODO </b> It would be better to put the Clover database in the "build" directory
     * but this would require giving the student code read/write/create permission
     * inside the build directory, which we might not want to grant.
     */
    public static final String CLOVER_DB = "clover.db";
    
    /** Key for using code coverage. */
    public static final String CODE_COVERAGE = "perform.code.coverage";
    
    /**
     * The key for finding the shell to use when launching C/Ruby/OCaml programs.
     */
    public static final String SHELL = "shell";
    
    /**
     * Key for using an unprivileged account to run untrusted student code.
     */
    public static final String UNPRIVILEGED_ACCOUNT = "unprivileged.account";
}
