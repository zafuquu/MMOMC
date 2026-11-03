package com.zafuquu.mmomc;

import java.util.Map;
import java.util.Set;

public final class Job {
    private Job() {}

    private static final Set<String> FIRST_JOBS = Set.of(
            "swordsman", "tank", "archer", "magician", "assist"
    );

    private static final Map<String, Set<String>> SPECIALIZATIONS = Map.of(
            "swordsman", Set.of("blade_master", "berserker"),
            "tank", Set.of("paladin", "guardian"),
            "archer", Set.of("ranger", "sharpshooter"),
            "magician", Set.of("elementor", "psykeeper"),
            "assist", Set.of("ringmaster", "billposter")
    );

    public static boolean isFirstJob(String id) {
        return id != null && FIRST_JOBS.contains(id.toLowerCase(java.util.Locale.ROOT));
    }

    public static boolean specializationBelongsTo(String firstJob, String specialization) {
        if (firstJob == null || specialization == null) return false;
        return SPECIALIZATIONS.getOrDefault(firstJob.toLowerCase(java.util.Locale.ROOT), Set.of())
                .contains(specialization.toLowerCase(java.util.Locale.ROOT));
    }
}
