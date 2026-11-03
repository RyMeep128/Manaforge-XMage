package org.mage.test.cards.manaforge;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.game.permanent.Permanent;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestPlayerBase;
import static org.junit.Assert.*;

public class ImmortalObligationTest extends CardTestPlayerBase {
    private void setup() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 7);
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.GRAVEYARD, playerB, "Silvercoat Lion");
        addCard(Zone.HAND, playerA, "Immortal Obligation");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Immortal Obligation", "Silvercoat Lion");
    }
    private void finish(int turn, PhaseStep step) {
        setStrictChooseMode(true);
        setStopAt(turn, step);
        execute();
    }
    @Test public void returnsUnderOpponentsControlWithCounterAndRestrictions() {
        setup();
        finish(1, PhaseStep.BEGIN_COMBAT);
        assertPermanentCount(playerB, "Silvercoat Lion", 1);
        assertCounterCount(playerB, "Silvercoat Lion", "duty", 1);
        Permanent lion = getPermanent("Silvercoat Lion");
        assertTrue(lion.getGoadingPlayers().contains(playerA.getId()));
        assertFalse(lion.canBlock(getPermanent("Grizzly Bears").getId(), currentGame));
    }
    @Test public void cannotAttackCasterEvenInDuel() {
        setup();
        finish(2, PhaseStep.DECLARE_ATTACKERS);
        assertFalse(getPermanent("Silvercoat Lion").canAttack(playerA.getId(), currentGame));
    }
    @Test public void removingCounterEndsRestrictions() {
        setup();
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        addCard(Zone.HAND, playerA, "Aether Snap");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Aether Snap");
        finish(1, PhaseStep.BEGIN_COMBAT);
        assertCounterCount(playerB, "Silvercoat Lion", "duty", 0);
        Permanent lion = getPermanent("Silvercoat Lion");
        assertFalse(lion.getGoadingPlayers().contains(playerA.getId()));
        assertTrue(lion.canBlock(getPermanent("Grizzly Bears").getId(), currentGame));
    }
    @Test public void blinkedCreatureIsANewObjectWithoutRestrictions() {
        setup();
        addCard(Zone.HAND, playerA, "Flicker");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Flicker", "Silvercoat Lion");
        finish(1, PhaseStep.BEGIN_COMBAT);
        assertCounterCount(playerB, "Silvercoat Lion", "duty", 0);
        Permanent lion = getPermanent("Silvercoat Lion");
        assertFalse(lion.getGoadingPlayers().contains(playerA.getId()));
        assertTrue(lion.canBlock(getPermanent("Grizzly Bears").getId(), currentGame));
    }
    @Test public void restrictionsRemainAfterControlChanges() {
        setup();
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.HAND, playerA, "Mind Control");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Mind Control", "Silvercoat Lion");
        finish(1, PhaseStep.BEGIN_COMBAT);
        assertPermanentCount(playerA, "Silvercoat Lion", 1);
        assertCounterCount(playerA, "Silvercoat Lion", "duty", 1);
        assertTrue(getPermanent("Silvercoat Lion").getGoadingPlayers().contains(playerA.getId()));
    }
    @Test public void replacingCounterDoesNotRestartExpiredEffects() {
        setup();
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        addCard(Zone.HAND, playerA, "Aether Snap");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Aether Snap");
        finish(1, PhaseStep.BEGIN_COMBAT);
        mage.cards.Card original = playerA.getGraveyard().getCards(currentGame).stream()
                .filter(c -> c.getName().equals("Immortal Obligation")).findFirst().get();
        Permanent lion = getPermanent("Silvercoat Lion");
        lion.addCounters(new mage.counters.Counter("duty", 1), original.getSpellAbility(), currentGame);
        currentGame.applyEffects();
        assertFalse(lion.getGoadingPlayers().contains(playerA.getId()));
        assertTrue(lion.canBlock(getPermanent("Grizzly Bears").getId(), currentGame));
    }
}
