package com.matcher.platform.service;

import com.matcher.platform.dto.response.CompanyMatchResponse;
import com.matcher.platform.dto.response.SubjectGapDetail;
import com.matcher.platform.entity.*;
import com.matcher.platform.entity.enums.CompanyVerificationStatus;
import com.matcher.platform.entity.enums.MatchType;
import com.matcher.platform.entity.enums.SkillProficiency;
import com.matcher.platform.repository.HiringCriteriaRepository;
import com.matcher.platform.repository.StudentProfileRepository;
import com.matcher.platform.util.StringNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MatchingEngineService {

    private static final Logger log = LoggerFactory.getLogger(MatchingEngineService.class);
    private static final int STRICT_MATCH_THRESHOLD = 3;

    private final HiringCriteriaRepository hiringCriteriaRepository;
    private final StudentProfileRepository studentProfileRepository;

    public MatchingEngineService(HiringCriteriaRepository hiringCriteriaRepository, StudentProfileRepository studentProfileRepository) {
        this.hiringCriteriaRepository = hiringCriteriaRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<CompanyMatchResponse> calculateMatchesForStudent(String studentEmail) {
        String normalizedEmail = studentEmail.trim().toLowerCase();
        Optional<StudentProfile> studentOpt = studentProfileRepository.findWithDetailsByEmail(normalizedEmail)
                .or(() -> studentProfileRepository.findByUserEmail(normalizedEmail));

        if (studentOpt.isEmpty()) {
            return Collections.emptyList();
        }

        StudentProfile student = studentOpt.get();

        List<HiringCriteria> allActiveCriteria = hiringCriteriaRepository.findAllActiveWithDetails();
        if (allActiveCriteria.isEmpty()) {
            return Collections.emptyList();
        }

        List<StudentAcademicRecord> academicRecords = student.getAcademicRecords() != null ? student.getAcademicRecords() : Collections.emptyList();
        List<StudentSkill> studentSkills = student.getSkills() != null ? student.getSkills() : Collections.emptyList();

        double totalMarks = 0.0;
        int subjectCount = 0;
        boolean hasUnverifiedMarks = false;

        for (StudentAcademicRecord record : academicRecords) {
            totalMarks += record.getEffectiveMark();
            subjectCount++;
            if (!Boolean.TRUE.equals(record.getIsVerified())) {
                hasUnverifiedMarks = true;
            }
        }
        double studentAggregate = subjectCount > 0 ? (totalMarks / subjectCount) : 0.0;

        List<CompanyMatchResponse> strictMatches = new ArrayList<>();
        List<CompanyMatchResponse> candidateRelaxedMatches = new ArrayList<>();

        for (HiringCriteria criteria : allActiveCriteria) {
            CompanyProfile company = criteria.getCompany();
            EvaluationResult eval = evaluateCriteria(criteria, academicRecords, studentSkills, studentAggregate, hasUnverifiedMarks);

            CompanyMatchResponse matchResponse = CompanyMatchResponse.builder()
                    .companyId(company.getId())
                    .companyName(company.getCompanyName())
                    .logoUrl(company.getLogoUrl())
                    .location(company.getLocation())
                    .companyVerificationStatus(company.getVerificationStatus() != null ? company.getVerificationStatus() : CompanyVerificationStatus.NOT_VERIFIED)
                    .roleTitle(criteria.getRoleTitle())
                    .matchScore(eval.score)
                    .matchType(eval.isStrictMatch ? MatchType.STRICT : MatchType.RELAXED_WEIGHTED)
                    .isVerificationPending(eval.isPendingVerification)
                    .verificationRemark(eval.isPendingVerification ? "Verification by Teacher is Required" : null)
                    .matchedSkills(eval.matchedSkills)
                    .missingSkills(eval.missingSkills)
                    .subjectGaps(eval.subjectGaps)
                    .academicGapSummary(eval.primaryGapSummary)
                    .build();

            if (eval.isStrictMatch) {
                strictMatches.add(matchResponse);
            } else {
                candidateRelaxedMatches.add(matchResponse);
            }
        }

        // Sort strict matches by match score descending
        strictMatches.sort(Comparator.comparingDouble(CompanyMatchResponse::getMatchScore).reversed());

        // Requirement: If student is qualified in fewer than 3 companies with strict filtering,
        // show closest possible matches with weighted score and gap remarks.
        if (strictMatches.size() >= STRICT_MATCH_THRESHOLD) {
            return strictMatches;
        }

        // Fallback: Combine strict matches + top closest weighted matches
        candidateRelaxedMatches.sort(Comparator.comparingDouble(CompanyMatchResponse::getMatchScore).reversed());

        List<CompanyMatchResponse> combinedResults = new ArrayList<>(strictMatches);
        int needed = Math.max(STRICT_MATCH_THRESHOLD, 5) - strictMatches.size();
        for (int i = 0; i < Math.min(needed, candidateRelaxedMatches.size()); i++) {
            combinedResults.add(candidateRelaxedMatches.get(i));
        }

        return combinedResults;
    }

    private EvaluationResult evaluateCriteria(
            HiringCriteria criteria,
            List<StudentAcademicRecord> studentMarks,
            List<StudentSkill> studentSkills,
            double studentAggregate,
            boolean globalUnverified
    ) {
        boolean isStrictMatch = true;
        boolean criteriaPendingVerification = false;
        List<SubjectGapDetail> subjectGaps = new ArrayList<>();
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        double totalSkillWeight = 0.0;
        double matchedSkillWeight = 0.0;

        // 1. Evaluate Skills (Typo-Tolerant & Canonical Matching)
        if (criteria.getRequiredSkills() != null && !criteria.getRequiredSkills().isEmpty()) {
            for (CriteriaRequiredSkill reqSkill : criteria.getRequiredSkills()) {
                String reqSkillName = reqSkill.getSkill() != null ? reqSkill.getSkill().getName() : "";
                double weight = reqSkill.getWeightage() != null ? reqSkill.getWeightage() : 1.0;
                totalSkillWeight += weight;

                Optional<StudentSkill> matchedStudentSkill = studentSkills.stream()
                        .filter(ss -> ss.getSkill() != null && StringNormalizer.isFuzzyMatch(ss.getSkill().getName(), reqSkillName))
                        .findFirst();

                if (matchedStudentSkill.isPresent()) {
                    StudentSkill ss = matchedStudentSkill.get();
                    if (isProficiencySufficient(ss.getProficiency(), reqSkill.getMinProficiency())) {
                        matchedSkills.add(reqSkillName);
                        matchedSkillWeight += weight;
                    } else {
                        missingSkills.add(reqSkillName + " (Requires " + reqSkill.getMinProficiency() + ")");
                        if (Boolean.TRUE.equals(reqSkill.getIsMandatory())) {
                            isStrictMatch = false;
                        }
                    }
                } else {
                    missingSkills.add(reqSkillName);
                    if (Boolean.TRUE.equals(reqSkill.getIsMandatory())) {
                        isStrictMatch = false;
                    }
                }
            }
        }

        double skillScorePercent = totalSkillWeight > 0 ? (matchedSkillWeight / totalSkillWeight) * 100.0 : 100.0;

        // 2. Evaluate Subject Cutoffs (Typo-Tolerant & Canonical Matching)
        double totalAcademicRatio = 0.0;
        int cutoffCount = 0;

        if (criteria.getSubjectCutoffs() != null && !criteria.getSubjectCutoffs().isEmpty()) {
            for (CriteriaSubjectCutoff cutoff : criteria.getSubjectCutoffs()) {
                cutoffCount++;
                String cutoffSubjectName = cutoff.getSubjectName();
                double requiredScore = cutoff.getMinMarksCutoff() != null ? cutoff.getMinMarksCutoff() : 0.0;

                Optional<StudentAcademicRecord> matchedRecord = studentMarks.stream()
                        .filter(r -> r.getSubjectName() != null && StringNormalizer.isFuzzyMatch(r.getSubjectName(), cutoffSubjectName))
                        .findFirst();

                if (matchedRecord.isPresent()) {
                    StudentAcademicRecord record = matchedRecord.get();
                    double actualScore = record.getEffectiveMark();
                    if (!Boolean.TRUE.equals(record.getIsVerified())) {
                        criteriaPendingVerification = true;
                    }

                    if (actualScore < requiredScore) {
                        double deficit = requiredScore - actualScore;
                        String gapRemark = String.format("A %.1f score more is required in %s", deficit, cutoff.getSubjectName());
                        subjectGaps.add(new SubjectGapDetail(cutoff.getSubjectName(), actualScore, requiredScore, deficit, gapRemark));
                        if (Boolean.TRUE.equals(cutoff.getIsMandatory())) {
                            isStrictMatch = false;
                        }
                        totalAcademicRatio += Math.max(0.0, actualScore / (requiredScore > 0 ? requiredScore : 100.0));
                    } else {
                        totalAcademicRatio += 1.0;
                    }
                } else {
                    // Student has not reported this subject mark yet
                    double deficit = requiredScore;
                    String gapRemark = String.format("A %.1f score more is required in %s", deficit, cutoff.getSubjectName());
                    subjectGaps.add(new SubjectGapDetail(cutoff.getSubjectName(), 0.0, requiredScore, deficit, gapRemark));
                    if (Boolean.TRUE.equals(cutoff.getIsMandatory())) {
                        isStrictMatch = false;
                    }
                }
            }
        }

        // 3. Overall Aggregate Percentage check
        if (criteria.getMinOverallPercentage() != null && criteria.getMinOverallPercentage() > 0) {
            if (studentAggregate < criteria.getMinOverallPercentage()) {
                isStrictMatch = false;
                double deficit = criteria.getMinOverallPercentage() - studentAggregate;
                subjectGaps.add(new SubjectGapDetail(
                        "Overall Aggregate",
                        Math.round(studentAggregate * 10.0) / 10.0,
                        criteria.getMinOverallPercentage(),
                        Math.round(deficit * 10.0) / 10.0,
                        String.format("An overall aggregate improvement of %.1f%% is required", deficit)
                ));
            }
        }

        double academicScorePercent = cutoffCount > 0 ? (totalAcademicRatio / cutoffCount) * 100.0 : 100.0;
        double weightedScore = Math.round(((0.60 * skillScorePercent) + (0.40 * academicScorePercent)) * 10.0) / 10.0;

        String primaryGapSummary = null;
        if (!subjectGaps.isEmpty()) {
            primaryGapSummary = subjectGaps.get(0).getGapRemark();
        }

        EvaluationResult result = new EvaluationResult();
        result.isStrictMatch = isStrictMatch;
        result.score = isStrictMatch ? 100.0 : Math.min(99.0, weightedScore);
        result.isPendingVerification = criteriaPendingVerification || globalUnverified;
        result.matchedSkills = matchedSkills;
        result.missingSkills = missingSkills;
        result.subjectGaps = subjectGaps;
        result.primaryGapSummary = primaryGapSummary;
        return result;
    }

    private boolean isProficiencySufficient(SkillProficiency studentProficiency, SkillProficiency requiredProficiency) {
        if (studentProficiency == null) return false;
        if (requiredProficiency == null) return true;
        return studentProficiency.ordinal() >= requiredProficiency.ordinal();
    }

    private static class EvaluationResult {
        boolean isStrictMatch;
        double score;
        boolean isPendingVerification;
        List<String> matchedSkills;
        List<String> missingSkills;
        List<SubjectGapDetail> subjectGaps;
        String primaryGapSummary;
    }
}
