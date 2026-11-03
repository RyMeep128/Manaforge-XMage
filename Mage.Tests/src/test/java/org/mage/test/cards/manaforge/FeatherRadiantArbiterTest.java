package org.mage.test.cards.manaforge;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

public class FeatherRadiantArbiterTest extends CardTestPlayerBase {
    private void setup(String spell, int genericMana) {
        addCard(Zone.BATTLEFIELD, playerA, "Feather, Radiant Arbiter");
        addCard(Zone.BATTLEFIELD, playerA, "Silvercoat Lion");
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerA, "Forest");
        if (genericMana > 0) addCard(Zone.BATTLEFIELD, playerA, "Plains", genericMana);
        addCard(Zone.HAND, playerA, spell);
    }
    private void finish() {
        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.BEGIN_COMBAT);
        execute();
    }
    @Test public void copiesForChosenOwnAndOpposingCreatures() {
        setup("Giant Growth", 4);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Giant Growth", "Feather, Radiant Arbiter");
        setChoice(playerA, "Silvercoat Lion^Grizzly Bears");
        setChoice(playerA, true);
        setChoice(playerA, TestPlayer.CHOICE_SKIP); // order the copies
        finish();
        assertPowerToughness(playerA, "Feather, Radiant Arbiter", 7, 6);
        assertPowerToughness(playerA, "Silvercoat Lion", 5, 5);
        assertPowerToughness(playerB, "Grizzly Bears", 5, 5);
        assertGraveyardCount(playerA, "Giant Growth", 1);
    }
    @Test public void choosingZeroDoesNotPayOrCopy() {
        setup("Giant Growth", 0);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Giant Growth", "Feather, Radiant Arbiter");
        setChoice(playerA, TestPlayer.CHOICE_SKIP);
        finish();
        assertPowerToughness(playerA, "Feather, Radiant Arbiter", 7, 6);
        assertPowerToughness(playerA, "Silvercoat Lion", 2, 2);
    }
    @Test public void mayDeclinePaymentAfterChoosing() {
        setup("Giant Growth", 2);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Giant Growth", "Feather, Radiant Arbiter");
        setChoice(playerA, "Silvercoat Lion");
        setChoice(playerA, TestPlayer.CHOICE_SKIP);
        setChoice(playerA, false);
        finish();
        assertPowerToughness(playerA, "Silvercoat Lion", 2, 2);
        assertTappedCount("Plains", false, 2);
    }
    @Test public void insufficientManaDoesNotProduceCopies() {
        setup("Giant Growth", 0);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Giant Growth", "Feather, Radiant Arbiter");
        setChoice(playerA, "Silvercoat Lion");
        setChoice(playerA, TestPlayer.CHOICE_SKIP);
        setChoice(playerA, true);
        finish();
        assertPowerToughness(playerA, "Silvercoat Lion", 2, 2);
    }
    @Test public void copiedAuraBecomesPermanent() {
        setup("Holy Strength", 3);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Holy Strength", "Feather, Radiant Arbiter");
        setChoice(playerA, "Silvercoat Lion");
        setChoice(playerA, TestPlayer.CHOICE_SKIP);
        setChoice(playerA, true);
        finish();
        assertPowerToughness(playerA, "Feather, Radiant Arbiter", 5, 5);
        assertPowerToughness(playerA, "Silvercoat Lion", 3, 4);
        assertPermanentCount(playerA, "Holy Strength", 2);
    }
    @Test public void spellTargetingAnotherCreatureDoesNotTrigger() {
        setup("Giant Growth", 0);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Giant Growth", "Silvercoat Lion");
        finish();
        assertPowerToughness(playerA, "Feather, Radiant Arbiter", 4, 3);
        assertPowerToughness(playerA, "Silvercoat Lion", 5, 5);
    }
    @Test public void opponentCastingAtFeatherDoesNotTrigger() {
        setup("Giant Growth", 0);
        addCard(Zone.BATTLEFIELD, playerB, "Forest");
        addCard(Zone.HAND, playerB, "Giant Growth");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerB, "Giant Growth", "Feather, Radiant Arbiter");
        finish();
        assertPowerToughness(playerA, "Silvercoat Lion", 2, 2);
    }
    @Test public void multiTargetSpellDoesNotTrigger() {
        setup("Seeds of Strength", 1);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Seeds of Strength", "Feather, Radiant Arbiter^Silvercoat Lion^Grizzly Bears");
        finish();
        assertPowerToughness(playerA, "Silvercoat Lion", 3, 3);
        assertPowerToughness(playerB, "Grizzly Bears", 3, 3);
    }
    @Test public void onlyCreaturesOriginalSpellCouldTargetAreOffered() {
        setup("Blossoming Defense", 2);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Blossoming Defense", "Feather, Radiant Arbiter");
        setChoice(playerA, "Silvercoat Lion");
        setChoice(playerA, true);
        finish();
        assertPowerToughness(playerA, "Silvercoat Lion", 4, 4);
        assertPowerToughness(playerB, "Grizzly Bears", 2, 2);
    }
}
