/**
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
 * Copyright (C) ITECH, University of Washington, Seattle WA.  All Rights Reserved.
 *
 */
package org.openelisglobal.testreflex.action.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.validator.GenericValidator;
import org.openelisglobal.analysis.service.AnalysisService;
import org.openelisglobal.analysis.valueholder.Analysis;
import org.openelisglobal.analyte.service.AnalyteService;
import org.openelisglobal.analyte.valueholder.Analyte;
import org.openelisglobal.note.service.NoteService;
import org.openelisglobal.note.service.NoteServiceImpl.NoteType;
import org.openelisglobal.note.valueholder.Note;
import org.openelisglobal.observationhistory.service.ObservationHistoryService;
import org.openelisglobal.observationhistory.valueholder.ObservationHistory;
import org.openelisglobal.result.service.ResultService;
import org.openelisglobal.result.valueholder.Result;
import org.openelisglobal.sample.valueholder.Sample;
import org.openelisglobal.scriptlet.service.ScriptletService;
import org.openelisglobal.scriptlet.valueholder.Scriptlet;
import org.openelisglobal.spring.util.SpringContext;
import org.openelisglobal.test.service.TestServiceImpl;
import org.openelisglobal.testreflex.service.TestReflexService;
import org.openelisglobal.testreflex.valueholder.TestReflex;
import org.openelisglobal.testresult.service.TestResultService;
import org.openelisglobal.testresult.valueholder.TestResult;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service
@DependsOn({ "springContext" })
public class TestReflexUtil {
    private static final String USER_CHOOSE_FLAG = "UC";

    private static String CONCLUSION_ANAYLETE_ID = null;
    private static Analyte CD4_ANAYLETE = null;
    private static String CD4_SCRIPTLET_ID = null;
    private static Set<String> TRIGGERING_REFLEX_TEST_IDS;
    private static Set<String> TRIGGERING_UC_REFLEX_TEST_IDS;
    private static Map<String, List<TestReflex>> TEST_TO_REFLEX_MAP;

    private static ObservationHistoryService observationService = SpringContext
            .getBean(ObservationHistoryService.class);
    private static ResultService resultService = SpringContext
            .getBean(org.openelisglobal.result.service.ResultService.class);
    private static TestResultService testResultService = SpringContext.getBean(TestResultService.class);
    private static AnalysisService analysisService = SpringContext.getBean(AnalysisService.class);
    private static TestReflexService testReflexService = SpringContext.getBean(TestReflexService.class);
    private static AnalyteService analyteService = SpringContext.getBean(AnalyteService.class);
    private static ScriptletService scriptletService = SpringContext.getBean(ScriptletService.class);
    private static NoteService noteService = SpringContext.getBean(NoteService.class);

    private TestReflexResolver reflexResolver = SpringContext.getBean(TestReflexResolver.class);

    static {
        Analyte analyte = new Analyte();
        analyte.setAnalyteName("Conclusion");
        CONCLUSION_ANAYLETE_ID = analyteService.getAnalyteByName(analyte, false).getId();
        analyte.setAnalyteName("generated CD4 Count");
        CD4_ANAYLETE = analyteService.getAnalyteByName(analyte, false);

        Scriptlet scriptlet = new Scriptlet();
        scriptlet.setScriptletName("Calculate CD4");
        scriptlet = scriptletService.getScriptletByName(scriptlet);
        if (!(scriptlet == null || scriptlet.getId() == null)) {
            CD4_SCRIPTLET_ID = scriptlet.getId();
        }

        TRIGGERING_REFLEX_TEST_IDS = new HashSet<>();
        TRIGGERING_UC_REFLEX_TEST_IDS = new HashSet<>();
        TEST_TO_REFLEX_MAP = new HashMap<>();

        List<TestReflex> reflexes = SpringContext.getBean(TestReflexService.class).getAllTestReflexs();

        for (TestReflex testReflex : reflexes) {
            String testId = testReflex.getTest().getId();
            List<TestReflex> reflexValues = TEST_TO_REFLEX_MAP.get(testId);
            if (reflexValues == null) {
                reflexValues = new ArrayList<>();
                TEST_TO_REFLEX_MAP.put(testId, reflexValues);
            }

            reflexValues.add(testReflex);
            TRIGGERING_REFLEX_TEST_IDS.add(testId);
            if (isUserChoiceReflex(testReflex)) {
                TRIGGERING_UC_REFLEX_TEST_IDS.add(testReflex.getTest().getId());
            }
        }
    }

    public static boolean isTriggeringReflexTestId(String testId) {
        return TRIGGERING_REFLEX_TEST_IDS.contains(testId);
    }

    public static boolean isTriggeringUserChoiceReflexTestId(String testId) {
        return TRIGGERING_UC_REFLEX_TEST_IDS.contains(testId);
    }

    public static boolean isUserChoiceReflex(TestReflex reflex) {
        return USER_CHOOSE_FLAG.equals(reflex.getFlags());
    }

    public static boolean testIsTriggeringReflexWithSibs(String testId) {
        if (isTriggeringReflexTestId(testId)) {
            List<TestReflex> reflexes = TEST_TO_REFLEX_MAP.get(testId);

            for (TestReflex reflex : reflexes) {
                if (!GenericValidator.isBlankOrNull(reflex.getSiblingReflexId())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static String makeReflexTestName(TestReflex testReflex) {
        return TestServiceImpl.getUserLocalizedTestName(testReflex.getAddedTest());
    }

    public static String makeReflexScriptName(TestReflex testReflex) {
        return testReflex.getActionScriptlet().getScriptletName();
    }

    public static String makeReflexTestValue(TestReflex testReflex) {
        return "test_" + testReflex.getAddedTest().getId();
    }

    public static String makeReflexScriptValue(TestReflex testReflex) {
        return "script_" + testReflex.getActionScriptletId();
    }

    public List<TestReflex> getSiblings(TestReflex reflex) {
        List<TestReflex> siblingList = new ArrayList<>();

        if (reflex.getSiblingReflexId() != null) {
            List<String> visitedReflexIdList = new ArrayList<>();
            visitedReflexIdList.add(reflex.getId());

            TestReflex currentReflex = reflex;

            while (true) {
                String siblingId = currentReflex.getSiblingReflexId();

                if (siblingId == null || visitedReflexIdList.contains(siblingId)) {
                    break;
                }

                currentReflex = new TestReflex();
                currentReflex.setId(siblingId);
                testReflexService.getData(currentReflex);

                if (GenericValidator.isBlankOrNull(currentReflex.getId())) {
                    break;
                } else {
                    siblingList.add(currentReflex);
                    visitedReflexIdList.add(siblingId);
                }
            }

        }
        return siblingList;
    }

    /*
     * This will find all the possible reflexes for a test. The intended use is to
     * mark a results row as possibly generating a reflex for which the user will
     * have to select the reflex action (either a conclusion or another test)
     */
    public List<TestReflex> getPossibleUserChoiceTestReflexsForTest(String testId) {
        return testReflexService.getTestReflexsByTestAndFlag(testId, USER_CHOOSE_FLAG);
    }

    /*
     * Gets the test reflex associated with this test and result.
     */
    public List<TestReflex> getTestReflexsForDictioanryResultTestId(String dictionaryId, String testId,
            boolean userChoiceOnly) {
        if (GenericValidator.isBlankOrNull(dictionaryId) || GenericValidator.isBlankOrNull(testId)) {
            return new ArrayList<>();
        }

        TestResult testResult = testResultService.getTestResultsByTestAndDictonaryResult(testId, dictionaryId);

        if (testResult == null) {
            return new ArrayList<>();
        }

        return userChoiceOnly ? testReflexService.getFlaggedTestReflexesByTestResult(testResult, USER_CHOOSE_FLAG)
                : testReflexService.getTestReflexesByTestResult(testResult);
    }

    public List<Analysis> addNewTestsToDBForReflexTests(List<TestReflexBean> newResults, String sysUserId)
            throws IllegalStateException {
        if (sysUserId == null) {
            throw new IllegalStateException("sysUserId not set");
        }

        /*
         * There are several use cases 1. A single result triggers a single reflex 2. A
         * single result triggers more than one reflex 3. Multiple new results triggers
         * a single reflex 4. A mixture of new result and a previous results triggers a
         * single reflex 5. Multiple new results trigger a more than one reflex 6. A
         * mixture of new and previous results trigger more than one reflex 7. A single
         * result forced the user to select reflexes 8. A mixture of new and previous
         * results forced the user to select reflexes 9. Multiselect results forced the
         * user to select reflexes for each result 10. A scriptlet determines what the
         * next test will be
         */

        /*
         * We want to handle results with user selections first
         */
        Collections.sort(newResults, new Comparator<TestReflexBean>() {

            @Override
            public int compare(TestReflexBean o1, TestReflexBean o2) {
                if (o1.getTriggersToSelectedReflexesMap().isEmpty()) {
                    return o2.getTriggersToSelectedReflexesMap().isEmpty() ? 0 : 1;
                } else {
                    return o2.getTriggersToSelectedReflexesMap().isEmpty() ? -1 : 0;
                }
            }
        });

        /*
         * For each sample we want to track which testReflexes have already been
         * evaluated. If we don't track them and two parents are being updated then we
         * would trigger the same reflex twice
         */
        Map<String, List<String>> sampleIdToHandledTestReflexIds = new HashMap<>();

        // keep track of analysis which have triggered reflexes
        List<Analysis> parentAnalysisList = new ArrayList<>();
        List<Analysis> reflexAnalysises = new ArrayList<>();

        for (TestReflexBean reflexBean : newResults) {
            // list may be empty or have previous handled reflexes
            List<String> handledReflexIdList = getHandledReflexesForSample(sampleIdToHandledTestReflexIds, reflexBean);

            // use cases 1-6, 10
            if (reflexBean.getTriggersToSelectedReflexesMap().isEmpty()) {
                reflexAnalysises.addAll(
                        handleAutomaticReflexes(parentAnalysisList, reflexBean, handledReflexIdList, sysUserId));
            } else { // use cases 7,8,9
                reflexAnalysises.addAll(handleUserSelectedReflexes(parentAnalysisList, reflexBean, sysUserId));

            }
        }
        return reflexAnalysises;
    }

    private List<Analysis> handleUserSelectedReflexes(List<Analysis> parentAnalysisList, TestReflexBean reflexBean,
            String sysUserId) {
        // The reflexes and the triggering tests have already been identified by
        // TestReflexUserChoiceProvider, if all of the parents are not being
        // picked up fix it there

        List<Analysis> reflexAnalysises = new ArrayList<>();
        for (String triggeringTests : reflexBean.getTriggersToSelectedReflexesMap().keySet()) {
            List<String> addedActionIds = reflexBean.getTriggersToSelectedReflexesMap().get(triggeringTests);
            // no reflexes triggered so no parents
            parentAnalysisList.clear();

            for (String addedActionId : addedActionIds) {
                TestReflex reflex = new TestReflex();
                reflex.setFlags(USER_CHOOSE_FLAG);
                reflex.setTestId(triggeringTests.split("_")[0]);
                String[] splitActionId = addedActionId.split("_");
                if ("test".equals(splitActionId[0])) {
                    reflex.setAddedTestId(splitActionId[1]);
                } else {
                    reflex.setActionScriptletId(splitActionId[1]);
                }

                Optional<Analysis> newAnalysis = addReflexTest(reflex, reflexBean.getResult(),
                        reflexBean.getPatient().getId(), reflexBean.getSample(), true, true, addedActionId, false,
                        sysUserId);
                if (newAnalysis.isPresent()) {
                    reflexAnalysises.add(newAnalysis.get());
                }

            }

            if (reflexBean.getResult().getAnalysis() != null) {
                parentAnalysisList.add(reflexBean.getResult().getAnalysis());
            }

            markSibAnalysisAsParent(parentAnalysisList, sysUserId);
        }
        return reflexAnalysises;
    }

    private List<Analysis> handleAutomaticReflexes(List<Analysis> parentAnalysisList, TestReflexBean reflexBean,
            List<String> handledReflexIdList, String sysUserId) {
        // More than one reflex may be returned if more than one action
        // should be taken by the result
        List<TestReflex> reflexesForResult = reflexResolver.getTestReflexesForResult(reflexBean.getResult());
        List<Analysis> reflexAnalysises = new ArrayList<>();
        for (TestReflex reflexForResult : reflexesForResult) {
            // filter out handled reflexes
            if (!GenericValidator.isBlankOrNull(reflexForResult.getSiblingReflexId())
                    && handledReflexIdList.contains(reflexForResult.getId())) {
                continue;
            }

            handledReflexIdList.add(reflexForResult.getId());

            List<TestReflex> siblingsOfResultReflex = getSiblings(reflexForResult);

            // no reflexes triggered so no parents
            parentAnalysisList.clear();

            // side effect of populating parent list and
            // handledRefleIdList
            boolean siblingsSatisfied = checkIfSiblingsSatisfiedAndPopulateParentList(parentAnalysisList, reflexBean,
                    handledReflexIdList, siblingsOfResultReflex);

            // All the conditions are satisfied so we can handle the
            // reflexes
            if (siblingsSatisfied) {
                boolean allSibAnalysisCausedReflex = doAllAnalysisHaveReflex(parentAnalysisList, reflexBean);

                Optional<Analysis> newAnalysis = addReflexTest(reflexForResult, reflexBean.getResult(),
                        reflexBean.getPatient().getId(), reflexBean.getSample(), true, true, null,
                        allSibAnalysisCausedReflex, sysUserId);
                if (newAnalysis.isPresent()) {
                    reflexAnalysises.add(newAnalysis.get());
                }
                // there may be multiple reflexes
                for (TestReflex siblingReflex : siblingsOfResultReflex) {
                    // we want to make sure we don't add the same test
                    // or observation twice
                    boolean addTest = siblingReflex.getAddedTestId() != null
                            && !siblingReflex.getAddedTestId().equals(reflexForResult.getAddedTestId());
                    boolean handleAction = siblingReflex.getActionScriptletId() != null
                            && !siblingReflex.getActionScriptletId().equals(reflexForResult.getActionScriptletId());

                    newAnalysis = addReflexTest(siblingReflex, reflexBean.getResult(), reflexBean.getPatient().getId(),
                            reflexBean.getSample(), addTest, handleAction, null, allSibAnalysisCausedReflex, sysUserId);
                    if (newAnalysis.isPresent()) {
                        reflexAnalysises.add(newAnalysis.get());
                    }
                }

                if (reflexBean.getResult().getAnalysis() != null) {
                    parentAnalysisList.add(reflexBean.getResult().getAnalysis());
                }

                markSibAnalysisAsParent(parentAnalysisList, sysUserId);
            }
        }
        return reflexAnalysises;
    }

    private boolean doAllAnalysisHaveReflex(List<Analysis> parentAnalysisList, TestReflexBean reflexBean) {
        if (reflexBean.getResult().getAnalysis() == null
                || !reflexBean.getResult().getAnalysis().getTriggeredReflex()) {
            return false;
        }

        for (Analysis analysis : parentAnalysisList) {
            if (!analysis.getTriggeredReflex()) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkIfSiblingsSatisfiedAndPopulateParentList(List<Analysis> parentAnalysisList,
            TestReflexBean resultSet, List<String> handledReflexIdList, List<TestReflex> siblingsOfResultReflex) {
        boolean siblingsSatisfied = true;

        for (TestReflex siblingReflex : siblingsOfResultReflex) {
            // mark the sibling as being handled, it may also be on the
            // list
            handledReflexIdList.add(siblingReflex.getId());

            if (siblingsSatisfied) {
                if (!reflexResolver.isSatisfied(siblingReflex, resultSet.getSample())) {
                    siblingsSatisfied = false;
                    parentAnalysisList.clear();
                    // we're not breaking out of the loop to let the rest of the
                    // siblings be added to the handled list
                } else if (reflexResolver.getLastValidAnalysis() != null) {
                    parentAnalysisList.add(reflexResolver.getLastValidAnalysis());
                }
            }
        }

        return siblingsSatisfied;
    }

    protected List<String> getHandledReflexesForSample(Map<String, List<String>> handledReflexsBySample,
            TestReflexBean resultSet) {
        List<String> handledReflexIdList = handledReflexsBySample.get(resultSet.getSample().getId());
        if (handledReflexIdList == null) {
            handledReflexIdList = new ArrayList<>();
            handledReflexsBySample.put(resultSet.getSample().getId(), handledReflexIdList);
        }
        return handledReflexIdList;
    }

    private Optional<Analysis> addReflexTest(TestReflex reflex, Result result, String patientId, Sample sample,
            boolean addTest, boolean handleAction, String actionSelectionId, boolean failOnDuplicateTest,
            String sysUserId) {

        if (addTest || handleAction) {

            ReflexAction reflexAction = reflexResolver.getReflexAction();
            reflexAction.handleReflex(reflex, result, actionSelectionId);

            ObservationHistory observation = reflexAction.getObservation();

            if (observation != null && handleAction) {
                observation.setPatientId(patientId);
                observation.setSampleId(sample.getId());
                observation.setSysUserId(sysUserId);
                observationService.insert(observation);
            }

            Analysis newAnalysis = reflexAction.getNewAnalysis();
            Result finalResult = reflexAction.getFinalResult();

            /*******
             * This is allowing duplicate tests to be added for CD4 absolute. If the
             * previous CD4 absolute was attached to a different analysis from the CD4 %
             * then this fails. This also precludes updates
             */
            if (failOnDuplicateTest && testDoneForSample(newAnalysis)) {
                return Optional.empty();
            }

            if (finalResult != null) {
                finalResult.setAnalysis(result.getAnalysis());
                finalResult.setSysUserId(sysUserId);
                if (finalResult.getId() == null) {
                    resultService.insert(finalResult);
                } else {
                    resultService.update(finalResult);
                }
            }

            if (newAnalysis != null && addTest) {
                Analysis currentAnalysis = result.getAnalysis();

                newAnalysis.setSysUserId(sysUserId);
                currentAnalysis.setSysUserId(sysUserId);
                currentAnalysis.setTriggeredReflex(Boolean.TRUE);

                analysisService.insert(newAnalysis);
                analysisService.update(currentAnalysis);

                List<Note> notes = new ArrayList<>();
                notes.add(noteService.createSavableNote(newAnalysis, NoteType.INTERNAL,
                        "Triggered by " + currentAnalysis.getTest().getLocalizedReportingName().getLocalizedValue(),
                        "Reflex Test Note", "1"));
                notes.add(noteService.createSavableNote(newAnalysis, NoteType.INTERNAL,
                        "This is part of a set of tests, please ensure all tests are resulted before validation",
                        "Reflex Test Note", "1"));
                if (result.getParentResult() == null) {
                    Note note = noteService.createSavableNote(currentAnalysis, NoteType.INTERNAL,
                            "This is part of a set of tests, please ensure all tests are resulted before validation",
                            "Reflex Test Note", "1");
                    if (!noteService.duplicateNoteExists(note)) {
                        notes.add(note);
                    }
                }
                noteService.saveAll(notes);
                return Optional.of(newAnalysis);
            }
        }
        return Optional.empty();
    }

    private boolean testDoneForSample(Analysis newAnalysis) {
        if (newAnalysis == null) {
            return false;
        }

        String newTestId = newAnalysis.getTest().getId();

        Sample sample = newAnalysis.getSampleItem().getSample();

        List<Analysis> analysisList = analysisService.getAnalysesBySampleId(sample.getId());

        for (Analysis analysis : analysisList) {
            if (duplicateTest(newTestId, analysis, newAnalysis)) {
                return true;
            }
        }

        return false;
    }

    private boolean duplicateTest(String newTestId, Analysis existingAnalysis, Analysis newAnalysis) {
        return newTestId.equals(existingAnalysis.getTest().getId())
                && existingAnalysis.getSampleTypeName().equals(newAnalysis.getSampleTypeName());
    }

    private void markSibAnalysisAsParent(List<Analysis> parentAnalysisList, String sysUserId) {
        for (Analysis analysis : parentAnalysisList) {
            analysis.setSysUserId(sysUserId);
            analysis.setTriggeredReflex(Boolean.TRUE);
//            analysisService.update(analysis);
        }

    }

    public void updateModifiedReflexes(List<TestReflexBean> reflexBeanList, String sysUserId)
            throws IllegalStateException {
        if (sysUserId == null) {
            throw new IllegalStateException("sysUserId not set");
        }
        /*
         * N.B. currently we are only updating calculated values and conclusions This
         * means only reflexes which have scriptlets as their action.
         */

        /*
         * The scenarios we need to support 1. The conclusion does not yet exist 2. The
         * conclusion does exist but one of the determining results has changed 3. The
         * conclusion does exist and has been changed by the user
         *
         * If both 2 and 3 exist then 3 wins
         */

        /*
         * the general processing flow will be to; 1. create groups of results by sample
         * 2. go through each group and ignore groups in which none of the results have
         * scriptlet reflexes 3. Ignore the group if one of the results is a conclusion
         * 4. find if there is an existing conclusion for the reflex 5. Either update or
         * modify the conclusion if the reflex is satisfied
         */

        Map<Sample, List<TestReflexBean>> groupedResults = groupBySample(reflexBeanList);

        for (List<TestReflexBean> reflexList : groupedResults.values()) {
            TestReflex scriptletReflex = getScriptletReflex(reflexList);

            if (scriptletReflex != null) {
                if (noConclusionInModifiedResult(reflexList)) {
                    List<Result> resultList = resultService.getResultsForSample(reflexList.get(0).getSample());

                    // We're Unfortunately hard coding some business rules here
                    if (CD4_SCRIPTLET_ID.equals(scriptletReflex.getActionScriptlet().getId())) {
                        RetroCIReflexActions reflexAction = new RetroCIReflexActions();
                        Result calculatedResults = reflexAction.getCD4CalculationResult(reflexList.get(0).getSample());

                        if (calculatedResults != null) {

                            Result cd4Result = null;
                            for (Result result : resultList) {
                                if (result.getAnalyte() != null
                                        && CD4_ANAYLETE.getId().equals(result.getAnalyte().getId())) {
                                    cd4Result = result;
                                    cd4Result.setValue(calculatedResults.getValue());
                                    break;
                                }
                            }

                            if (cd4Result == null) {
                                cd4Result = calculatedResults;
                            }

                            cd4Result.setSysUserId(sysUserId);

                            if (cd4Result.getId() == null) {
                                resultService.insert(cd4Result);
                            } else {
                                resultService.update(cd4Result);
                            }
                        }
                    } // else It is a HIV conclusion
                }
            }
        }

    }

    private Map<Sample, List<TestReflexBean>> groupBySample(List<TestReflexBean> reflexBeanList) {
        Map<Sample, List<TestReflexBean>> groupedBeans = new HashMap<>();

        for (TestReflexBean bean : reflexBeanList) {
            List<TestReflexBean> beanList = groupedBeans.get(bean.getSample());

            if (beanList == null) {
                beanList = new ArrayList<>();
                groupedBeans.put(bean.getSample(), beanList);
            }

            beanList.add(bean);
        }

        return groupedBeans;
    }

    private TestReflex getScriptletReflex(List<TestReflexBean> reflexBeanList) {
        for (TestReflexBean bean : reflexBeanList) {
            List<TestReflex> reflexList = reflexResolver.getTestReflexesForResult(bean.getResult());

            if (!reflexList.isEmpty()) {
                for (TestReflex testReflex : reflexList) {
                    if (testReflex.getActionScriptlet() != null) {
                        return testReflex;
                    }
                }
            }
        }

        return null;
    }

    private boolean noConclusionInModifiedResult(List<TestReflexBean> reflexBeanList) {

        for (TestReflexBean bean : reflexBeanList) {
            if (isConclusion(bean.getResult())) {
                return true;
            }
        }
        return true;
    }

    private boolean isConclusion(Result result) {
        Analyte analyte = result.getAnalyte();

        return analyte != null
                && (CONCLUSION_ANAYLETE_ID.equals(analyte.getId()) || CD4_ANAYLETE.getId().equals(analyte.getId()));

    }

}
