
/*
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations under
* the License.
*
* The Original Code is OpenELIS code.
*
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*
* Contributor(s): CIRG, University of Washington, Seattle WA.
*/

/**
 * Cote d'Ivoire
 * @author pahill
 * @since 2010-06-25
 */
package org.openelisglobal.patient.saving;

import static org.openelisglobal.common.services.StatusService.RecordStatus.InitialRegistration;
import static org.openelisglobal.common.services.StatusService.RecordStatus.NotRegistered;

import javax.servlet.http.HttpServletRequest;

import org.openelisglobal.common.services.IStatusService;
import org.openelisglobal.patient.form.PatientEntryByProjectForm;
import org.openelisglobal.patient.util.PatientUtil;
import org.openelisglobal.samplehuman.valueholder.SampleHuman;
import org.openelisglobal.spring.util.SpringContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class PatientEntryAfterAnalyzer extends PatientEntry implements IPatientEntryAfterAnalyzer {

    public PatientEntryAfterAnalyzer(PatientEntryByProjectForm form, String sysUserId, HttpServletRequest request) {
        this();
        super.setFieldsFromForm(form);
        super.setSysUserId(sysUserId);
        super.setRequest(request);
    }

    public PatientEntryAfterAnalyzer() {
        super();
        newPatientStatus = InitialRegistration;
        newSampleStatus = NotRegistered;
    }

    @Override
    public boolean canAccession() {
        return (NotRegistered == statusSet.getPatientRecordStatus()
                && NotRegistered == statusSet.getSampleRecordStatus());
    }

    /**
     * Find existing sampleHuman, so we can update it with our new patient when we
     * fill in all IDs when we persist.
     *
     * @see org.openelisglobal.patient.saving.PatientEntry#populateSampleHuman()
     */
    @Override
    protected void populateSampleHuman() {
        sampleHuman = new SampleHuman();
        sampleHuman.setSampleId(statusSet.getSampleId());
        sampleHumanService.getDataBySample(sampleHuman);
    }

    /**
     * Get rid of the links to the UNKNOWN_PATIENT, then insert a new one.
     *
     * @see org.openelisglobal.patient.saving.Accessioner#persistRecordStatus()
     */
    @Override
    protected void persistRecordStatus() {
        SpringContext.getBean(IStatusService.class).deleteRecordStatus(sample, PatientUtil.getUnknownPatient(),
                sysUserId);
        super.persistRecordStatus();
    }
}
