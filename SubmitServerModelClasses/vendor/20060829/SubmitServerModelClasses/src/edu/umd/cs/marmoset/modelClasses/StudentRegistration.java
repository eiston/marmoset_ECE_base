/*
 * Created on Aug 30, 2004
 */
package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Object to represent a row of the student_registration table.
 * TODO Refactor getFromPreparedStatement().
 * @author daveho
 */
public class StudentRegistration implements Comparable {
    public static final Comparator<StudentRegistration> cvsAccountComparator = new Comparator<StudentRegistration> () {

        public int compare(StudentRegistration s1, StudentRegistration s2) {
   
            int result = compareInstructorStatus(s1, s2);
            if (result != 0) return result;
            result = compareAccountNames(s1, s2);
            if (result != 0) return result;
            result = compareNames(s1, s2);
            return result;

        }
        
    };
    public static final Comparator<StudentRegistration> nameComparator = new Comparator<StudentRegistration> () {

        public int compare(StudentRegistration s1, StudentRegistration s2) {
        	  
            int result = compareInstructorStatus(s1, s2);
            if (result != 0) return result;
            result = compareNames(s1, s2);
            if (result != 0) return result;
            result = compareAccountNames(s1, s2);
            return result;

        }
        
    };
    
    public static Comparator<StudentRegistration> getComparator(String sortKey) {
        if ("account".equals(sortKey)) return cvsAccountComparator;
        return nameComparator; 
    }
    public static final Comparator<StudentRegistration> getSubmissionViaMappedValuesComparator(final Map values) {
        return  new Comparator<StudentRegistration>() {
    

        public int compare(StudentRegistration s1, StudentRegistration s2) {

            
        	Comparable o1  =  (Comparable) values.get(s1.getStudentRegistrationPK());
        	Comparable  o2 =   (Comparable) values.get(s2.getStudentRegistrationPK());
            
            int result =  compareValues(o1, o2);
            if (result != 0) return result;
            return nameComparator.compare(s1, s2);
        }
        };
        
    };
   
    public static final Comparator<StudentRegistration> getSubmissionViaTimestampComparator(final Map values) {
        return  new Comparator<StudentRegistration>() {
    

        public int compare(StudentRegistration s1, StudentRegistration s2) {
            
            Submission o1  = (Submission) values.get(s1.getStudentRegistrationPK());
            Submission o2 =  (Submission) values.get(s2.getStudentRegistrationPK());
            int result =  compareNulls(o1, o2);
            if (result != 0) return result;
            if (o1 == null) return nameComparator.compare(s1, s2);
            result =  - compareValues(o1.getSubmissionTimestamp(), o2.getSubmissionTimestamp());
            if (result != 0) return result;
            return nameComparator.compare(s1, s2);
        }
        };
        
    };
    private static int compareInstructorStatus(StudentRegistration s1, StudentRegistration s2) {
        return -(s1.getInstructorLevel() - s2.getInstructorLevel());
    }
    private static int compareNames(StudentRegistration s1, StudentRegistration s2) {
        int result = s1.lastname.compareTo(s2.lastname);
        if (result != 0) return result;
        return s1.firstname.compareTo(s2.firstname);
    }
    private static int compareAccountNames(StudentRegistration s1, StudentRegistration s2) {
        return s1.cvsAccount.compareTo(s2.cvsAccount);
    }
	public static final String TABLE_NAME = "student_registration";
    private String studentRegistrationPK;
	private String coursePK;
	private String studentPK;
	private String cvsAccount;
	private String instructorCapability;
    private String firstname;
    private String lastname;

	/**
	 * List of all attributes of student_registration table.
	 */
	  static final String[] ATTRIBUTE_NAME_LIST = {
		"student_registration_pk",
		"course_pk",
		"student_pk",
		"cvs_account",
        "instructor_capability",
        "firstname",
        "lastname"
	};
	
	/**
	 * Fully-qualified attributes for student_registration table.
	 */
	public static final String ATTRIBUTES =
	    Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);
    public static final String READ_ONLY_CAPABILITY = "read-only";
    public static final String MODIFY_CAPABILITY = "modify";
    public static final String CANONICAL_CAPABILITY = "canonical";
    public static final int READ_ONLY_CAPABILITY_LEVEL = 2;
    public static final int MODIFY_CAPABILITY_LEVEL = 3;
    public static final int CANONICAL_CAPABILITY_LEVEL = 1;
    public static final int STUDENT_CAPABILITY_LEVEL = 0;
    public static final String ADD_PERMISSION = "add";
    public static final String REMOVE_PERMISSION = "remove";

	/**
	 * @return Returns the coursePK.
	 */
	public String getCoursePK() {
		return coursePK;
	}
	/**
	 * @param coursePK The coursePK to set.
	 */
	public void setCoursePK(String coursePK) {
		this.coursePK = coursePK;
	}
	/**
	 * @return Returns the cvsAccount.
	 */
	public String getCvsAccount() {
		return cvsAccount;
	}
	/**
	 * @param cvsAccount The cvsAccount to set.
	 */
	public void setCvsAccount(String cvsAccount) {
		this.cvsAccount = cvsAccount;
	}
    /**
     * @return Returns the instructorCapability.
     */
    public String getInstructorCapability()
    {
        return instructorCapability;
    }
    /**
     * Instructor level is defined at:<br>
     * 3: modify capability (can create/edit projects, and make other modifications backed 
     * by a write to the database.<br>
     * 2: read-only capability (can access read-only instructor capabilities)
     * 1: canonical capability (the instructors canonical account, which can submit reference solutions)
     * 0: no instrictor privileges, can only access student resources.
     * @return Returns the instructorLevel.
     */
    public int getInstructorLevel()
    {
    	if (MODIFY_CAPABILITY.equals(instructorCapability)) return MODIFY_CAPABILITY_LEVEL;
        if (READ_ONLY_CAPABILITY.equals(instructorCapability)) return READ_ONLY_CAPABILITY_LEVEL;
        if (CANONICAL_CAPABILITY.equals(instructorCapability)) return CANONICAL_CAPABILITY_LEVEL;
        return STUDENT_CAPABILITY_LEVEL;
    }
    
    public boolean isCanonical() {
    	return CANONICAL_CAPABILITY.equals(instructorCapability);
    }
    
    /**
     * @param instructorCapability The instructorCapability to set.
     */
    public void setInstructorCapability(String instructorCapability)
    {
        this.instructorCapability = instructorCapability;
    }
	/**
	 * @return Returns the studentPK.
	 */
	public String getStudentPK() {
		return studentPK;
	}
	/**
	 * @param studentPK The studentPK to set.
	 */
	public void setStudentPK(String studentPK) {
		this.studentPK = studentPK;
	}
	/**
	 * @return Returns the studentRegistrationPK.
	 */
	public String getStudentRegistrationPK() {
		return studentRegistrationPK;
	}
	/**
	 * @param studentRegistrationPK The studentRegistrationPK to set.
	 */
	public void setStudentRegistrationPK(String studentRegistrationPK) {
		this.studentRegistrationPK = studentRegistrationPK;
	}
	
    /**
     * @return Returns the firstname.
     */
    public String getFirstname() {
        return firstname;
    }
    /**
     * @param firstname The firstname to set.
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }
    /**
     * @return Returns the lastname.
     */
    public String getLastname() {
        return lastname;
    }
    /**
     * @param lastname The lastname to set.
     */
    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    
	public int fetchValues(ResultSet resultSet, int startingFrom) throws SQLException {
		setStudentRegistrationPK(resultSet.getString(startingFrom++));
		setCoursePK(resultSet.getString(startingFrom++));
		setStudentPK(resultSet.getString(startingFrom++));
		setCvsAccount(resultSet.getString(startingFrom++));
		setInstructorCapability(resultSet.getString(startingFrom++));
        setFirstname(resultSet.getString(startingFrom++));
        setLastname(resultSet.getString(startingFrom++));
		return startingFrom;
	}
	
	/**
	 * Insert a new row into the student_registration database using
	 * the provided connection.
	 * 
	 * THIS DOES NOT CHECK FOR DUPLICATES.
	 * 
	 * @param conn
	 * @throws SQLException
	 */
	public void insert(Connection conn)
	throws SQLException
	{
	    String query = "INSERT INTO student_registration " +
	    "VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";
	    
	    PreparedStatement stmt = null;
	    try {
	        stmt = conn.prepareStatement(query);
	        
	        putValues(stmt, 1);
	        
	        stmt.executeUpdate();
	        
	        // XXX The last insert ID is only valid inside a transaction
	        setStudentRegistrationPK(Queries.lastInsertId(conn));
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}
	
	/**
	 * Update a StudentRegistration record in the database.
	 * @param conn The connection to the database.
	 * @throws SQLException
	 */
	public void update(Connection conn)
	throws SQLException
	{
	    String update = Queries.makeUpdateStatementWithWhereClause(
	            ATTRIBUTE_NAME_LIST,
	            TABLE_NAME,
	            " WHERE student_registration_pk = ? ");
	    
	    PreparedStatement stmt = null;
	    try {
	        stmt = conn.prepareStatement(update);

	        int index = putValues(stmt, 1);
	        stmt.setString(index, getStudentRegistrationPK());
	        
	        stmt.executeUpdate();
	        
	    } finally {
	        Queries.closeStatement(stmt);
	    }
	}
	
	/**
	 * Puts the fields of this object (but not the first field, which is the primary key)
	 * into a prepared statement starting at a given index.
	 * @param stmt the preparedStatement
	 * @param index the index to start at
	 * @return the index of the next open slot in the prepared statement
	 * @throws SQLException
	 */
	public int putValues(PreparedStatement stmt, int index)
	throws SQLException
	{
	    stmt.setString(index++, coursePK);
	    stmt.setString(index++, studentPK);
	    stmt.setString(index++, cvsAccount);
	    stmt.setString(index++, instructorCapability);
	    stmt.setString(index++, firstname);
	    stmt.setString(index++, lastname);
	    return index;
	}
	
	/**
	 * Looks up a student registation based on the cvs account and semester.
	 * This method should only called by the LogEclipseLaunchEvent servlet since it
	 * doesn't check for a password.
	 * @param cvsAccount
	 * @param semester
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
//	public static StudentRegistration lookupByCvsAccountAndSemester(String cvsAccount,
//			String semester,
//			Connection conn)
//	throws SQLException
//	{
//		String query = 
//			" SELECT " +ATTRIBUTES+
//			" FROM student_registration, courses " +
//			" WHERE student_registration.cvs_account = ? " +
//			" AND student_registration.course_pk = courses.course_pk " +
//			" AND courses.semester = ? ";
//		
//		PreparedStatement stmt = conn.prepareStatement(query);
//		stmt.setString(1, cvsAccount);
//		stmt.setString(2, semester);
//		return getFromPreparedStatement(stmt);
//	}
	
	/**
	 * Looks up a student registration row based on the cvs account and course PK.
	 * 
	 * @param cvsAccount the cvs account
	 * @param coursePK the PK of the course
	 * @param conn the connection to the database
	 * @return the StudentRegistration object if it's found; null otherwise
	 * @throws SQLException
	 */
	public static StudentRegistration lookupByCvsAccountAndCoursePK(String cvsAccount, String coursePK, Connection conn)
	throws SQLException
	{
	    String query = queryString("") + 
        " AND student_registration.cvs_account = ? "+
	    " AND student_registration.course_pk = ?";
	    
	    PreparedStatement stmt = conn.prepareStatement(query);
	    stmt.setString(1, cvsAccount);
	    stmt.setString(2, coursePK);
	        
	    return getFromPreparedStatement(stmt);
	}
	
	public Student getCorrespondingStudent(Connection conn)
	throws SQLException
	{
		Student student = Student.lookupByStudentPK(getStudentPK(), conn);
		if (student==null) {
        	throw new SQLException("Database is corrupted; studentRegistrationPK " +
        			getStudentPK()+ " exists, but I can't find " +
        			"corresponding student record with studentPK = "+getStudentPK());
        }
		return student;	
	}
	
	public static List<StudentRegistration> lookupAllByCoursePK(String coursePK, Connection conn)
	throws SQLException
	{
	    String query = 
	        " SELECT " +ATTRIBUTES+
	        " FROM student_registration " +
	        " WHERE course_pk = ? ";
	    
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, coursePK);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
	}
	
	/**
	 * Fetches all studentRegistration records that have at least one submission
	 * for the project with the given projectPK.
	 * @param projectPK Primary Key of the project.
	 * @param conn Connection to the database.
	 * @return A list of studentRegistration records that have at least one submission
	 * 		for the given project.
	 * @throws SQLException
	 */
	public static List<StudentRegistration> lookupAllWithAtLeastOneSubmissionByProjectPK(
			String projectPK,
			Connection conn)
	throws SQLException
	{
	    String query = 
	        " SELECT " +ATTRIBUTES+
	        " FROM student_registration, student_submit_status " +
            " WHERE student_registration.student_registration_pk = student_submit_status.student_registration_pk " +
            " AND student_submit_status.project_pk = ? ";
	    
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, projectPK);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
	}
	
	public static List<StudentRegistration> lookupCanonicalAccountsByCoursePK(String coursePK, Connection conn)
	throws SQLException
	{
		String query = 
			" SELECT " +ATTRIBUTES+
			" FROM student_registration, students " +
			" WHERE course_pk = ? " +
			" AND students.student_pk = student_registration.student_pk " +
			" AND (instructor_capability = ? " +
			" 		OR students.superuser = ?) ";
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, coursePK);
			stmt.setString(2, CANONICAL_CAPABILITY);
			stmt.setString(3, "yes");
			return getAllFromPreparedStatement(stmt);
		} finally {
			Queries.closeStatement(stmt);
		}
	}
	
	public static List<StudentRegistration> lookupAllByStudentPK(String studentPK, Connection conn)
	throws SQLException
	{
	    String query = 
	        " SELECT " +ATTRIBUTES+
	        " FROM student_registration " +
	        " WHERE student_pk = ? ";
	    
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            stmt.setString(1, studentPK);
            return getAllFromPreparedStatement(stmt);
        } finally {
            Queries.closeStatement(stmt);
        }
	}
	
	private static List<StudentRegistration> getAllFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
	    ResultSet rs = stmt.executeQuery();
	    
	    List<StudentRegistration> collection = new LinkedList<StudentRegistration>();
	    while (rs.next())
	    {
	        StudentRegistration registration = new StudentRegistration();
	        registration.fetchValues(rs, 1);
	        collection.add(registration);
	    }
	    return collection;
    }
	
	public static StudentRegistration lookupBySubmissionPK(String submissionPK, Connection conn)
	throws SQLException
	{
	    String query = queryString(", " + Submission.TABLE_NAME) + 
	        " AND student_registration.student_registration_pk = submissions.student_registration_pk " +
	        " AND submissions.submission_pk = ? ";
	    
	    PreparedStatement stmt = conn.prepareStatement(query);
	    stmt.setString(1, submissionPK);
	    
	    return getFromPreparedStatement(stmt);
	}
	
	public static StudentRegistration lookupByStudentRegistrationPK(String studentRegistrationPK, Connection conn)
	throws SQLException
	{
	    String query = queryString("") + " AND student_registration_pk = ? ";
	    
	    PreparedStatement stmt = conn.prepareStatement(query);
	    stmt.setString(1, studentRegistrationPK);
	    
	    return getFromPreparedStatement(stmt);
	}
	

	/**
	 * Looks up a student Registration based on the studentPK and the name of the course.
	 * 
	 * @param studentPK the PK of the student
	 * @param courseName the name of the course
	 * @param conn the connection to the database
	 * @return the StudentRegistration object if it's found; null otherwise
	 * @throws SQLException
	 */
	public static StudentRegistration lookupByStudentPKAndCourseName(
			String studentPK, String courseName, Connection conn)
		throws SQLException
	{
        String query = queryString(", courses") + 
        " AND student_registration.course_pk = courses.course_pk  "+
		" AND student_registration.student_pk = ? "+
		" AND courses.coursename= ?";
		
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, studentPK);
		stmt.setString(2, courseName);
			
		return getFromPreparedStatement(stmt);
	}
	
    static String queryString(String additionalTables) {
        return   "SELECT " +ATTRIBUTES+ "," + Student.TABLE_NAME + ".firstname, " + Student.TABLE_NAME + ".lastname" + " "+
        "FROM "+
        TABLE_NAME+", "+Student.TABLE_NAME + additionalTables + 
        " WHERE " +
            TABLE_NAME + ".student_pk = " + Student.TABLE_NAME + ".student_pk ";
       
    }
	/**
	 * Finds a student registration based on the student's PK and the course's PK.
	 * 
	 * @param studentPK the PK of the student
	 * @param coursePK the PK of the course
	 * @param conn the connection to the database
	 * @return the StudentRegistration object if it's found; null otherwise
	 * @throws SQLException
	 */
	public static StudentRegistration lookupByStudentPKAndCoursePK(
			String studentPK,
			String coursePK,
			Connection conn)
		throws SQLException
	{
		String query = queryString("") + 
		" AND student_registration.student_pk = ? "+
		" AND student_registration.course_pk = ?";
		
		PreparedStatement stmt = conn.prepareStatement(query);
		stmt.setString(1, studentPK);
		stmt.setString(2, coursePK);
		
		//Debug.print("lookupStudentRegistration...()" + stmt.toString());
		return getFromPreparedStatement(stmt);
	}
	
	public static StudentRegistration lookupByCvsAccountAndProjectPKAndOneTimePassword(
        String cvsAccount,
        String oneTimePassword,
        String projectPK,
        Connection conn)
    throws SQLException
    {
    	String query = queryString(",student_submit_status") + 
        " AND student_registration.cvs_account = ? " +
        " AND student_submit_status.one_time_password = ? " +
        " AND student_registration.student_registration_pk = student_submit_status.student_registration_pk " +
        " AND student_submit_status.project_pk = ? ";
    	
    	PreparedStatement stmt = conn.prepareStatement(query);
    		
    	stmt.setString(1, cvsAccount);
    	stmt.setString(2, oneTimePassword);
        stmt.setString(3, projectPK);

    	return getFromPreparedStatement(stmt);
    }
	
	/**
	 * Private helper method that executes a prepared statement, reads the results
	 * into a StudentRegistration object and then returns it.  Returns null if it can't
	 * find the StudentRegistration.  Closes the prepared statement if it's not null.
	 * 
	 * @param stmt the prepared statement
	 * @return the StudentRegistration object if it's found; null otherwise
	 * @throws SQLException
	 */
	private static StudentRegistration getFromPreparedStatement(PreparedStatement stmt)
	throws SQLException
	{
	    try {
	        ResultSet rs = stmt.executeQuery();
			
			if (rs.next())
			{
				StudentRegistration studentRegistration = new StudentRegistration();
				studentRegistration.fetchValues(rs, 1);
				return studentRegistration;
			}
			return null;
		} finally {
			Queries.closeStatement(stmt);
			// null out stmt since it has been closed
			stmt = null;
	    }
	}
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object that) {
        return this.cvsAccount.compareTo(((StudentRegistration)that).cvsAccount);
    }
    /**
     * @param o1
     * @param o2
     * @return
     */
    static int compareValues(Comparable o1, Comparable  o2) {
        int result = compareNulls(o1, o2);
        if (result != 0) return result;
        if (o1 == o2) return 0;
        return o1.compareTo(o2);

    }
    static int compareNulls(Object o1, Object o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;
        return 0;

    }
    
    public static Map<String, String> lookupStudentRegistrationMapByProjectPK(
        String projectPK,
        Connection conn)
    throws SQLException
    {
        String query=
            " SELECT " +ATTRIBUTES+
            " FROM student_registration, projects " +
            " WHERE projects.project_pk = ? " +
            " AND student_registration.course_pk = projects.course_pk ";
        Map<String,String> map=new HashMap<String,String>();
        PreparedStatement stmt=null;
        try {
            stmt=conn.prepareStatement(query);
            stmt.setString(1, projectPK);
            ResultSet rs=stmt.executeQuery();
            while (rs.next()) {
                StudentRegistration registration=new StudentRegistration();
                registration.fetchValues(rs, 1);
                map.put(registration.getStudentRegistrationPK(), registration.getCvsAccount());
            }
            return map;
        } finally {
            Queries.closeStatement(stmt);
        }
    }
}
