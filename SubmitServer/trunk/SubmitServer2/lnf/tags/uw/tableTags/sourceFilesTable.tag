<%@ taglib prefix="c" uri="http://jakarta.apache.org/taglibs/c"%>
<%@ taglib prefix="fmt" uri="http://jakarta.apache.org/taglibs/fmt"%>
<%@ taglib prefix="ss" uri="http://www.cs.umd.edu/marmoset/ss"%>

<h1>Source files in submission ${submission.submissionPK}</h1>
	
<c:if test="${testType != null and testNumber != null}">
	<h2>
	Coverage information for ${hybridTestType} ${testType} test #${testNumber}: ${testName}</h2>
</c:if>
<p>

<table>
<tr class="r${counter.index % 2}">
	<th> Filename </th>
	<c:if test="${userSession.capabilitiesActivated and testProperties.performCodeCoverage}">
	<th>coverage<br>statements</th>
	<th>coverage<br>conditionals</th>
	<th>coverage<br>methods</th>
	</c:if>
</tr>
<c:forEach var="sourceFileName" items="${sourceFileList}" varStatus="counter">
	<tr class="r${counter.index % 2}">
		<td>
			<%--
				Weirdness Alert:  the testType and testNumber can't go after sourceFileName;
				does this have anything to do with the slash being mis-encoded in the path
				to the source file name?
			--%>
			<c:set var="link" value="/view/sourceCode.jsp"/>
			<%--
			<c:if test="${instructorCapability == 'true'}">
				<c:set var="link" value="/view/instructor/sourceCode.jsp"/>
			</c:if>
			--%>
			<c:url var="sourceFileURL" value="${link}">
				<c:param name="submissionPK" value="${submission.submissionPK}" />
				<c:if test="${param.testType != null and param.testNumber != null}">
					<c:param name="testType" value="${param.testType}" />
					<c:param name="testNumber" value="${param.testNumber}" />
					<c:if test="${hybridTestType != null}">
					<c:param name="hybridTestType" value="${hybridTestType}" />
					</c:if>
				</c:if>
				<c:param name="sourceFileName" value="${sourceFileName}" />
			</c:url>
			<a href="${sourceFileURL}">${sourceFileName}</a>
		</td>
		<c:choose>
		<c:when test="${userSession.capabilitiesActivated and 
			filenameToCoverageStatsMap[sourceFileName] != null and
			(testProperties.language == 'Java' or testProperties.language=='java') and
			testProperties.performCodeCoverage == 'true'}">
			${filenameToCoverageStatsMap[sourceFileName].HTMLTableRow}
		</c:when>
		<c:otherwise>
			<td></td>
			<td></td>
			<td></td>
		</c:otherwise>
		</c:choose>
	</tr>
</c:forEach>
<c:if test="${testProperties.performCodeCoverage}">
	<tr class="r2">
	<td> <b> All files </b> <br>excluding JUnit tests</td>
	${codeCoverageResults.overallCoverageStats.HTMLTableRow}
	</tr>
</c:if>
	
</table>

<ss:submissionDetails />