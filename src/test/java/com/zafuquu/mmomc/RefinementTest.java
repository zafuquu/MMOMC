package com.zafuquu.mmomc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefinementTest {
    @Test
    void successChanceUsesTargetLevelBands() {
        assertEquals(100, RpgRefinement.successChancePercent(1, 0));
        assertEquals(90, RpgRefinement.successChancePercent(5, 0));
        assertEquals(75, RpgRefinement.successChancePercent(8, 0));
        assertEquals(55, RpgRefinement.successChancePercent(10, 0));
        assertEquals(35, RpgRefinement.successChancePercent(12, 0));
        assertEquals(20, RpgRefinement.successChancePercent(15, 0));
        assertEquals(10, RpgRefinement.successChancePercent(17, 0));
        assertEquals(5, RpgRefinement.successChancePercent(20, 0));
    }

    @Test
    void pityIncreasesChanceAndGuaranteesSuccessAtLimit() {
        assertEquals(10, RpgRefinement.successChancePercent(20, 1));
        assertEquals(50, RpgRefinement.successChancePercent(20, 9));
        assertEquals(100, RpgRefinement.successChancePercent(20, 10));
        assertEquals(100, RpgRefinement.successChancePercent(20, 100));
    }

    @Test
    void auraMilestonesAreExactlyFiveLevelsApart() {
        assertEquals(0, RpgRefinement.auraTier(0));
        assertEquals(0, RpgRefinement.auraTier(4));
        assertEquals(1, RpgRefinement.auraTier(5));
        assertEquals(2, RpgRefinement.auraTier(10));
        assertEquals(3, RpgRefinement.auraTier(15));
        assertEquals(4, RpgRefinement.auraTier(20));
    }

    @Test
    void statBonusScalesLinearlyAndRefinementStopsAtTwenty() {
        assertEquals(0, RpgRefinement.statBonusPercent(0));
        assertEquals(10, RpgRefinement.statBonusPercent(5));
        assertEquals(20, RpgRefinement.statBonusPercent(10));
        assertEquals(40, RpgRefinement.statBonusPercent(20));
        assertTrue(RpgRefinement.canRefine(19));
        assertFalse(RpgRefinement.canRefine(20));
    }

    @Test
    void attemptSuccessAdvancesLevelAndResetsPity() {
        var result = RpgRefinement.resolveAttempt(4, 3, 0);
        assertTrue(result.success());
        assertEquals(5, result.refinementLevel());
        assertEquals(0, result.consecutiveFailures());
        assertEquals(105 - 15, result.successChancePercent());
    }

    @Test
    void failedAttemptPreservesItemLevelAndBuildsPity() {
        var result = RpgRefinement.resolveAttempt(19, 2, 99);
        assertFalse(result.success());
        assertEquals(19, result.refinementLevel());
        assertEquals(3, result.consecutiveFailures());
        assertEquals(20, result.successChancePercent());
    }

    @Test
    void pityGuaranteesSuccessAndMaximumLevelCannotBeRefined() {
        var guaranteed = RpgRefinement.resolveAttempt(19, 10, 99);
        assertTrue(guaranteed.success());
        assertEquals(20, guaranteed.refinementLevel());
        assertEquals(0, guaranteed.consecutiveFailures());
        assertThrows(IllegalArgumentException.class, () -> RpgRefinement.resolveAttempt(20, 0, 0));
    }

    @Test
    void rejectsOutOfRangeInputs() {
        assertThrows(IllegalArgumentException.class, () -> RpgRefinement.successChancePercent(0, 0));
        assertThrows(IllegalArgumentException.class, () -> RpgRefinement.successChancePercent(21, 0));
        assertThrows(IllegalArgumentException.class, () -> RpgRefinement.successChancePercent(5, -1));
        assertThrows(IllegalArgumentException.class, () -> RpgRefinement.auraTier(21));
        assertThrows(IllegalArgumentException.class, () -> RpgRefinement.statBonusPercent(-1));
    }
}
