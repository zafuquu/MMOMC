package com.zafuquu.mmomc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PartyRewardsTest {
    @Test void dividesEvenly() {
        assertArrayEquals(new long[]{100, 100, 100}, RpgPartyRewards.split(300, 3));
    }
    @Test void distributesRemainderExactly() {
        long[] shares = RpgPartyRewards.split(10, 3);
        assertArrayEquals(new long[]{4, 3, 3}, shares);
        assertEquals(10, java.util.Arrays.stream(shares).sum());
    }
    @Test void smallRewardsDoNotInflate() {
        long[] shares = RpgPartyRewards.split(2, 5);
        assertArrayEquals(new long[]{1, 1, 0, 0, 0}, shares);
        assertEquals(2, java.util.Arrays.stream(shares).sum());
    }
    @Test void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> RpgPartyRewards.split(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> RpgPartyRewards.split(10, 0));
    }
}
