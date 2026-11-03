package com.zafuquu.mmomc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkillFormulaTest {
    @Test
    void physicalAndMagicScalingUseTheirPrimaryStats() {
        assertEquals(6.0, RpgSkillFormulas.specializationDamage(15, 6.0, 0.85));
        assertEquals(14.5, RpgSkillFormulas.specializationDamage(25, 6.0, 0.85));
        assertEquals(16.0, RpgSkillFormulas.specializationDamage(25, 6.0, 1.0));
    }

    @Test
    void basicSkillsScaleWithoutReducingBaseDamage() {
        assertEquals(5.0, RpgSkillFormulas.powerStrikeDamage(10));
        assertEquals(8.5, RpgSkillFormulas.powerStrikeDamage(25));
        assertEquals(4.0, RpgSkillFormulas.arcaneBoltDamage(10));
        assertEquals(10.0, RpgSkillFormulas.arcaneBoltDamage(25));
    }

    @Test
    void healingScalesByIntelligenceAndAssistRole() {
        assertEquals(6.0, RpgSkillFormulas.healAmount(15, false));
        assertEquals(10.0, RpgSkillFormulas.healAmount(15, true));
        assertEquals(8.0, RpgSkillFormulas.healAmount(25, false));
        assertEquals(14.0, RpgSkillFormulas.healAmount(25, true));
    }

    @Test
    void rejectsNegativeStatsAndInvalidMultipliers() {
        assertThrows(IllegalArgumentException.class, () -> RpgSkillFormulas.powerStrikeDamage(-1));
        assertThrows(IllegalArgumentException.class, () -> RpgSkillFormulas.specializationDamage(15, 6, -1));
    }
}
