package com.zafuquu.mmomc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobTest {
    @Test
    void recognizesAllFiveFirstJobs() {
        assertTrue(Job.isFirstJob("swordsman"));
        assertTrue(Job.isFirstJob("tank"));
        assertTrue(Job.isFirstJob("archer"));
        assertTrue(Job.isFirstJob("magician"));
        assertTrue(Job.isFirstJob("assist"));
        assertFalse(Job.isFirstJob("beginner"));
        assertFalse(Job.isFirstJob("necromancer"));
    }

    @Test
    void acceptsOnlySpecializationsForTheirParentJob() {
        assertTrue(Job.specializationBelongsTo("magician", "elementor"));
        assertTrue(Job.specializationBelongsTo("magician", "psykeeper"));
        assertTrue(Job.specializationBelongsTo("assist", "ringmaster"));
        assertTrue(Job.specializationBelongsTo("assist", "billposter"));

        assertFalse(Job.specializationBelongsTo("magician", "ringmaster"));
        assertFalse(Job.specializationBelongsTo("archer", "paladin"));
        assertFalse(Job.specializationBelongsTo("beginner", "elementor"));
    }

    @Test
    void everySpecializationMapsToExactlyItsIntendedFirstJob() {
        assertTrue(Job.specializationBelongsTo("swordsman", "blade_master"));
        assertTrue(Job.specializationBelongsTo("swordsman", "berserker"));
        assertTrue(Job.specializationBelongsTo("tank", "paladin"));
        assertTrue(Job.specializationBelongsTo("tank", "guardian"));
        assertTrue(Job.specializationBelongsTo("archer", "ranger"));
        assertTrue(Job.specializationBelongsTo("archer", "sharpshooter"));
        assertTrue(Job.specializationBelongsTo("magician", "elementor"));
        assertTrue(Job.specializationBelongsTo("magician", "psykeeper"));
        assertTrue(Job.specializationBelongsTo("assist", "ringmaster"));
        assertTrue(Job.specializationBelongsTo("assist", "billposter"));
    }

    @Test
    void manaCapacityScalesFromTheBaseIntelligenceWithoutNegativeCapacity() {
        assertEquals(50, RpgData.maxManaForInt(15));
        assertEquals(80, RpgData.maxManaForInt(25));
        assertEquals(50, RpgData.maxManaForInt(0));
    }

    @Test
    void jobIdsAreCaseInsensitive() {
        assertTrue(Job.isFirstJob("SWORDSMAN"));
        assertTrue(Job.specializationBelongsTo("Magician", "Elementor"));
    }
}
