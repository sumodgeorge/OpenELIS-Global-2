<%@ page language="java" contentType="text/html; charset=UTF-8"
	import="org.openelisglobal.common.action.IActionConstants,
                 org.openelisglobal.internationalization.MessageUtil"%>

<%@ page isELIgnored="false"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ taglib prefix="ajax" uri="/tags/ajaxtags"%>
<style>
.tab-label {
	overflow: hidden;
	border: 1px solid #ccc;
	background-color: #f1f1f1;
}

/* Style the buttons that are used to open the tab content */
.tab-label button {
	background-color: inherit;
	float: left;
	border: none;
	outline: none;
	cursor: pointer;
	padding: 14px 16px;
	transition: 0.3s;
}

/* Change background color of buttons on hover */
.tab-label button:hover {
	background-color: #ddd;
}

/* Create an active/current tablink class */
.tab-label button.active {
	background-color: #ccc;
}

/* Style the tab content */
.tabcontent {
	visibility: collapse;
	padding: 6px 12px;
	border: 1px solid #ccc;
	border-top: none;
}

.clientEmailRow {
	visibility: visible;
}
</style>


<script>
	var dirty = false;

	function makeDirty() {
		dirty = true;

		if (typeof (showSuccessMessage) !== 'undefined') {
			showSuccessMessage(false);
		}
		// Adds warning when leaving page if content has been entered into makeDirty form fields
		function formWarning() {
			return "<spring:message code="banner.menu.dataLossWarning"/>";
		}
		window.onbeforeunload = formWarning;
	}

	function enableEditSystemDefault() {
		var systemDefaults = document.getElementsByClassName("systemDefault");
		for (i = 0; i < systemDefaults.length; i++) {
			systemDefaults[i].disabled = false;
		}
		document.getElementById("editSystemDefaultPayloadTemplate").value = "true";
	}

	function openTab(event, tabClass) {
		// Declare all variables
		var i, tabcontent, tablinks, rows;

		// Get all elements with class="tabcontent" and hide them
		tabcontent = document.getElementsByClassName("tabcontent");
		for (i = 0; i < tabcontent.length; i++) {
			tabcontent[i].style.visibility = "collapse";
		}

		// Get all elements with class="tablinks" and remove the class "active"
		tablinks = document.getElementsByClassName("tablinks");
		for (i = 0; i < tablinks.length; i++) {
			tablinks[i].className = tablinks[i].className
					.replace(" active", "");
		}

		// Show the current tab, and add an "active" class to the button that opened the tab
		rows = document.getElementsByClassName(tabClass);
		for (i = 0; i < rows.length; i++) {
			rows[i].style.visibility = "visible";
		}
		event.currentTarget.className += " active";
	}

	function validateForm(form) {
		return true;
	}
</script>

<form:hidden path="config.id" />
<form:hidden path="config.test.id" />
<table width="70%">
	<tbody>
		<tr>
			<td>

				<h2>
					<c:out
						value="${form.config.test.localizedTestName.localizedValue}" />
				</h2> 
				<br> <spring:message code="" text="Client Email" /> <form:checkbox
					path="config.clientEmail.active"
					onChange="makeDirty();" /> <spring:message code=""
					text="Client SMS" /> <form:checkbox
					path="config.clientSMS.active"
					onChange="makeDirty();" /> <spring:message code=""
					text="Provider Email" /> <form:checkbox
					path="config.providerEmail.active"
					onChange="makeDirty();" /> <spring:message code=""
					text='Provider SMS' /> <form:checkbox
					path="config.providerSMS.active"
					onChange="makeDirty();" />
			</td>
		</tr>
		<tr>
			<td>
				<div id="instructions"
					style="background-color: rgb(200, 218, 218); padding: 20px;">
					<b>
						<h2 style="background-color: rgb(200, 218, 218)">Instructions:</h2>
						When a notification is created, it will first check if a message
						template is defined for the recipient/message type. <br>
					<br> If none exists, it will use the test default message. <br>
					<br> If that does not exist, it will use the system default
						message. <br>
					<br> Variables will be replaced by their corresponding values
						in the message body and the subject. <br>
					<br> Variables should always include the square brackets and
						should have no whitespace. <br>

						<h3 style="background-color: rgb(200, 218, 218)">Variable
							List:</h3> [testName] : Name of the test this is for<br>
						[testResult] : Result of the test<br> [patientFirstName] :
						Patient's first<br> [patientLastNameInitial] : Patient's last
						name's initial<br>

					</b>
				</div>
			</td>
		</tr>
		<!-- 	SYSTEM DEFAULT -->
		<tr>
			<td style="width: 100%;">
				<h2>
					<spring:message code="testnotification.systemdefault.template"
						text="System Default Message" />
					<form:hidden id="editSystemDefaultPayloadTemplate"
						path="editSystemDefaultPayloadTemplate" value="false" />
					<form:hidden path="systemDefaultPayloadTemplate.id" />
				</h2>
			</td>
		</tr>
		<tr>
			<td><spring:message code="testnotification.subjecttemplate"
					text="Subject" /> <form:input
					path="systemDefaultPayloadTemplate.subjectTemplate"
					class="systemDefault" onChange="makeDirty();" disabled="true" />

				<button style="float: right;" type="button"
					onClick="enableEditSystemDefault()">Edit</button></td>
		</tr>
		<tr>
			<td><spring:message code="testnotification.messagetemplate"
					text="Message" /></td>
		</tr>
		<tr>
			<td><form:textarea
					path="systemDefaultPayloadTemplate.messageTemplate" disabled="true"
					class="systemDefault" onChange="makeDirty();" cols="50" rows="10"
					style="overflow:scroll;" /></td>
		</tr>

		<!-- 		TEST DEFAULT -->
		<tr>
			<td>
				<h2>
					<spring:message code="testnotification.testdefault.template"
						text="Test Default Message" />
				</h2>
			</td>
		</tr>
		<tr>
			<td><spring:message code="testnotification.subjecttemplate"
					text="Subject" />
					<form:hidden path="config.defaultPayloadTemplate.type" value="TEST_RESULT"/>
					<form:input
					path="config.defaultPayloadTemplate.subjectTemplate"
					onChange="makeDirty();" /></td>
		</tr>
		<tr>
			<td><spring:message code="testnotification.messagetemplate"
					text="Message" /></td>
		</tr>
		<tr>
			<td><form:textarea
					path="config.defaultPayloadTemplate.messageTemplate"
					cols="50" rows="10" style="overflow:scroll;"
					onChange="makeDirty();" /></td>
		</tr>

		<!-- 		METHOD/PERSON SPECIFIC -->
		<tr>
			<td>
				<h2>
					<spring:message code="testnotification.options"
						text="Individual Messages" />
				</h2>
			</td>
		</tr>
		<tr>
			<td>
				<div class="tab-label">
					<button type="button" class="tablinks active"
						onclick="openTab(event, 'clientEmailRow')">Client Email</button>
					<button type="button" class="tablinks"
						onclick="openTab(event, 'clientSMSRow')">Client SMS</button>
					<button type="button" class="tablinks"
						onclick="openTab(event, 'providerEmailRow')">Provider
						Email</button>
					<button type="button" class="tablinks"
						onclick="openTab(event, 'providerSMSRow')">Provider SMS</button>
				</div>
			</td>
		</tr>

		<tr class="clientEmailRow tabcontent">
			<td>
				<h2>
					<spring:message code="testnotification.client.email.template"
						text="Email Message" />
				</h2>
			</td>
		</tr>
		<tr class="clientEmailRow tabcontent">
			<td><spring:message code="testnotification.subjecttemplate"
					text="Subject" /> 
					<form:hidden path="config.clientEmail.payloadTemplate.type" value="TEST_RESULT"/>
					<form:input
					path="config.clientEmail.payloadTemplate.subjectTemplate"
					onChange="makeDirty();" /></td>
		</tr>
		<tr class="clientEmailRow tabcontent">
			<td><spring:message code="testnotification.messagetemplate"
					text="Message" /></td>
		</tr>
		<tr class="clientEmailRow tabcontent">
			<td><form:textarea
					path="config.clientEmail.payloadTemplate.messageTemplate"
					cols="50" rows="10" style="overflow:scroll;"
					onChange="makeDirty();" /></td>
		</tr>
		<tr class="clientSMSRow tabcontent">
			<td>
				<h2>
					<spring:message code="testnotification.client.sms.template"
						text="SMS Message" />
				</h2>
			</td>
		</tr>
		<tr class="clientSMSRow tabcontent">
			<td><spring:message code="testnotification.subjecttemplate"
					text="Subject" /> 
					<form:hidden path="config.clientSMS.payloadTemplate.type" value="TEST_RESULT"/>
					<form:input
					path="config.clientSMS.payloadTemplate.subjectTemplate"
					onChange="makeDirty();" /></td>
		</tr>
		<tr class="clientSMSRow tabcontent">
			<td><spring:message code="testnotification.messagetemplate"
					text="Message" /></td>
		</tr>
		<tr class="clientSMSRow tabcontent">
			<td><form:textarea
					path="config.clientSMS.payloadTemplate.messageTemplate"
					cols="50" rows="10" style="overflow:scroll;"
					onChange="makeDirty();" /></td>
		</tr>


		<tr class="providerEmailRow tabcontent">
			<td>
				<h2>
					<spring:message code="testnotification.provider.email.template"
						text="Email Message" />
				</h2>
			</td>
		</tr>
		<tr class="providerEmailRow tabcontent">
			<td><spring:message code="testnotification.subjecttemplate"
					text="Subject" /> 
					<form:hidden path="config.providerEmail.payloadTemplate.type" value="TEST_RESULT"/>
					<form:input
					path="config.providerEmail.payloadTemplate.subjectTemplate"
					onChange="makeDirty();" /></td>
		</tr>
		<tr class="providerEmailRow tabcontent">
			<td><spring:message code="testnotification.messagetemplate"
					text="Message" /></td>
		</tr>
		<tr class="providerEmailRow tabcontent">
			<td><form:textarea
					path="config.providerEmail.payloadTemplate.messageTemplate"
					cols="50" rows="10" style="overflow:scroll;"
					onChange="makeDirty();" /></td>
		</tr>
		<tr class="providerSMSRow tabcontent">
			<td>
				<h2>
					<spring:message code="testnotification.provider.sms.template"
						text="SMS Message" />
				</h2>
			</td>
		</tr>
		<tr class="providerSMSRow tabcontent">
			<td><spring:message code="testnotification.subjecttemplate"
					text="Subject" /> 
					<form:hidden path="config.providerSMS.payloadTemplate.type" value="TEST_RESULT"/>
					<form:input
					path="config.providerSMS.payloadTemplate.subjectTemplate"
					onChange="makeDirty();" /></td>
		</tr>
		<tr class="providerSMSRow tabcontent">
			<td><spring:message code="testnotification.messagetemplate"
					text="Message" /></td>
		</tr>
		<tr class="providerSMSRow tabcontent">
			<td><form:textarea
					path="config.providerSMS.payloadTemplate.messageTemplate"
					cols="50" rows="10" style="overflow:scroll;"
					onChange="makeDirty();" /></td>
		</tr>



	</tbody>
</table>

