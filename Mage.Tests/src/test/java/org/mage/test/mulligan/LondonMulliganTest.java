package org.mage.test.mulligan;

import mage.game.mulligan.MulliganType;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/** Every redraw is offered before bottoming; only the finally kept hand is reduced. */
public class LondonMulliganTest extends MulliganTestBase {
    protected MulliganType getMullType() { return MulliganType.LONDON; }
    protected int getCardsPerMull() { return 7; }

    private void scenario(int free, int mulligans) {
        MulliganScenarioTest scenario = new MulliganScenarioTest(getMullType(), free);
        int keepSize = Math.max(0, 7 - Math.max(0, mulligans - free));
        Set<UUID> offered = new HashSet<>();
        List<UUID> bottomed = new ArrayList<>();
        for (int i = 0; i < mulligans; i++) {
            scenario.mulligan(() -> {
                scenario.assertSizes(7, 33);
                offered.clear(); offered.addAll(scenario.getHand());
                return true;
            });
        }
        if (keepSize > 0) {
            scenario.mulligan(() -> {
                scenario.assertSizes(7, 33);
                offered.clear(); offered.addAll(scenario.getHand());
                return false;
            });
        }
        for (int i = 0; i < 7 - keepSize; i++) {
            final int alreadyBottomed = i;
            scenario.discardBottom(count -> {
                assertEquals(1, count);
                scenario.assertSizes(7 - alreadyBottomed, 33 + alreadyBottomed);
                UUID selected = scenario.getHand().iterator().next();
                bottomed.add(selected);
                return Collections.singletonList(selected);
            });
        }
        scenario.run(() -> {
            scenario.assertSizes(keepSize, 40 - keepSize);
            if (keepSize > 0) {
                offered.removeAll(bottomed);
                assertEquals(offered, scenario.getHand());
            }
            assertEquals(new HashSet<>(bottomed), new HashSet<>(scenario.getNBottomOfLibrary(bottomed.size())));
        });
    }
    @Test public void repeatedMulligansOfferSevenBeforeAnyBottomChoice() {
        for (int mulligans = 0; mulligans <= 7; mulligans++) scenario(0, mulligans);
    }
    @Test public void freeMulliganPreservesSevenAndOnlyFinalKeepBottoms() {
        for (int mulligans = 0; mulligans <= 8; mulligans++) scenario(1, mulligans);
    }
    @Test public void keepingWithoutPaidMulligansDoesNotCapAnEnlargedHand() {
        for (int free : new int[]{0, 1}) {
            MulliganScenarioTest scenario = new MulliganScenarioTest(getMullType(), free);
            if (free > 0) scenario.mulligan(() -> true);
            scenario.mulligan(() -> {
                scenario.drawAdditionalCards(3);
                return false;
            });
            scenario.run(() -> scenario.assertSizes(10, 30));
        }
    }
}
