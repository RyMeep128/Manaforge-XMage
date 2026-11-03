package org.mage.test.cards.manaforge;

import mage.abilities.keyword.HasteAbility;
import mage.constants.*;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.watchers.Watcher;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;
import static org.junit.Assert.*;

public class HitItTest extends CardTestPlayerBase {
    private void setup() {
        skipInitShuffling();
        removeAllCardsFromLibrary(playerA);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 3);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 3);
        addCard(Zone.HAND, playerA, "Hit It!");
        currentGame.getState().addWatcher(new RevealAndMillWatcher());
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Hit It!");
    }
    private void finish() { setStrictChooseMode(true); setStopAt(1, PhaseStep.BEGIN_COMBAT); execute(); }
    @Test public void revealsFiveMustChooseALandAndPutsItTapped() {
        setup(); addCard(Zone.LIBRARY, playerA, "Island"); addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 4);
        addTarget(playerA, "Island"); finish();
        assertPermanentCount(playerA, "Island", 1); assertTappedCount("Island", true, 1);
        assertGraveyardCount(playerA, "Grizzly Bears", 4); assertLibraryCount(playerA, 0);
        assertEquals(5, currentGame.getState().getWatcher(RevealAndMillWatcher.class).maxRevealed);
        assertEquals(0, currentGame.getState().getWatcher(RevealAndMillWatcher.class).milled);
    }
    @Test public void noLandMovesAllRevealedCardsWithoutMilling() {
        setup(); addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 5); finish();
        assertGraveyardCount(playerA, "Grizzly Bears", 5);
        assertEquals(5, currentGame.getState().getWatcher(RevealAndMillWatcher.class).maxRevealed);
        assertEquals(0, currentGame.getState().getWatcher(RevealAndMillWatcher.class).milled);
    }
    @Test public void shortLibraryUsesOnlyRemainingCards() {
        setup(); addCard(Zone.LIBRARY, playerA, "Island"); addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 2);
        addTarget(playerA, "Island"); finish();
        assertGraveyardCount(playerA, "Grizzly Bears", 2); assertTappedCount("Island", true, 1);
        assertEquals(3, currentGame.getState().getWatcher(RevealAndMillWatcher.class).maxRevealed);
    }
    @Test public void grantsHasteOnlyToControllersCreatures() {
        setup(); addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 5);
        addCard(Zone.BATTLEFIELD, playerB, "Silvercoat Lion"); addCard(Zone.HAND, playerA, "Balduvian Bears");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Balduvian Bears");
        attack(1, playerA, "Balduvian Bears");
        setStrictChooseMode(true); setStopAt(1, PhaseStep.END_COMBAT); execute();
        assertLife(playerB, 18);
        assertAbility(playerA, "Balduvian Bears", HasteAbility.getInstance(), true);
        assertAbility(playerB, "Silvercoat Lion", HasteAbility.getInstance(), false);
    }
    @Test public void hasteEndsWhenEnchantmentLeaves() {
        setup(); addCard(Zone.LIBRARY, playerA, "Grizzly Bears", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion"); addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.HAND, playerA, "Disenchant"); waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Disenchant", "Hit It!"); finish();
        assertAbility(playerA, "Silvercoat Lion", HasteAbility.getInstance(), false);
    }
    public static class RevealAndMillWatcher extends Watcher {
        int maxRevealed, milled;
        public RevealAndMillWatcher() { super(WatcherScope.GAME); }
        @Override public void watch(GameEvent event, Game game) {
            int count = game.getState().getRevealed().values().stream().mapToInt(cards -> cards.size()).sum();
            maxRevealed = Math.max(maxRevealed, count);
            if (event.getType() == GameEvent.EventType.MILLED_CARD) milled++;
        }
    }
}
