package mage.cards.i;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.RestrictionEffect;
import mage.abilities.effects.common.combat.GoadTargetEffect;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.counters.Counter;
import mage.counters.Counters;
import mage.filter.common.FilterCreatureCard;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.common.TargetCardInOpponentsGraveyard;
import mage.target.targetpointer.FixedTarget;

/** Manaforge implementation. Effects follow one returned object's incarnation. */
public final class ImmortalObligation extends CardImpl {
    public ImmortalObligation(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.INSTANT}, "{1}{W}");
        getSpellAbility().addTarget(new TargetCardInOpponentsGraveyard(new FilterCreatureCard("creature card in an opponent's graveyard")));
        getSpellAbility().addEffect(new ImmortalObligationReturnEffect());
    }
    private ImmortalObligation(final ImmortalObligation card) { super(card); }
    @Override public ImmortalObligation copy() { return new ImmortalObligation(this); }
}

class ImmortalObligationReturnEffect extends OneShotEffect {
    ImmortalObligationReturnEffect() {
        super(Outcome.PutCreatureInPlay);
        staticText = "return target creature card from an opponent's graveyard to the battlefield under their control with a duty counter on it. For as long as that creature has a duty counter on it, it is goaded, can't attack you or a permanent you control, and can't block creatures you control";
    }
    private ImmortalObligationReturnEffect(final ImmortalObligationReturnEffect effect) { super(effect); }
    @Override public ImmortalObligationReturnEffect copy() { return new ImmortalObligationReturnEffect(this); }
    @Override public boolean apply(Game game, Ability source) {
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (card == null || game.getState().getZone(card.getId()) != Zone.GRAVEYARD) return false;
        Player owner = game.getPlayer(card.getOwnerId());
        if (owner == null) return false;
        Counters counters = new Counters();
        counters.addCounter(new Counter("duty", 1));
        game.setEnterWithCounters(card.getId(), counters);
        owner.moveCards(card, Zone.BATTLEFIELD, source, game);
        Permanent permanent = game.getPermanent(card.getId());
        if (permanent == null || permanent.getCounters(game).getCount("duty") == 0) return true;
        game.addEffect(new ImmortalObligationGoadEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
        game.addEffect(new ImmortalObligationRestrictionEffect().setTargetPointer(new FixedTarget(permanent, game)), source);
        return true;
    }
}

class ImmortalObligationGoadEffect extends GoadTargetEffect {
    ImmortalObligationGoadEffect() { super(Duration.Custom); }
    private ImmortalObligationGoadEffect(final ImmortalObligationGoadEffect effect) { super(effect); }
    @Override public ImmortalObligationGoadEffect copy() { return new ImmortalObligationGoadEffect(this); }
    @Override public boolean isInactive(Ability source, Game game) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        return super.isInactive(source, game) || permanent == null || permanent.getCounters(game).getCount("duty") == 0;
    }
    @Override public boolean apply(Game game, Ability source) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (permanent == null || permanent.getCounters(game).getCount("duty") == 0) { discard(); return true; }
        return super.apply(game, source);
    }
}

class ImmortalObligationRestrictionEffect extends RestrictionEffect {
    ImmortalObligationRestrictionEffect() { super(Duration.Custom); }
    private ImmortalObligationRestrictionEffect(final ImmortalObligationRestrictionEffect effect) { super(effect); }
    @Override public ImmortalObligationRestrictionEffect copy() { return new ImmortalObligationRestrictionEffect(this); }
    @Override public boolean isInactive(Ability source, Game game) {
        Permanent permanent = game.getPermanent(getTargetPointer().getFirst(game, source));
        return super.isInactive(source, game) || permanent == null || permanent.getCounters(game).getCount("duty") == 0;
    }
    @Override public boolean applies(Permanent permanent, Ability source, Game game) {
        Permanent affected = game.getPermanent(getTargetPointer().getFirst(game, source));
        if (affected == null || affected.getCounters(game).getCount("duty") == 0) { discard(); return false; }
        return affected.getId().equals(permanent.getId());
    }
    @Override public boolean canAttack(Permanent attacker, UUID defenderId, Ability source, Game game, boolean canUseChooseDialogs) {
        if (defenderId == null) return true;
        if (source.isControlledBy(defenderId)) return false;
        Permanent defender = game.getPermanent(defenderId);
        return defender == null || !defender.isControlledBy(source.getControllerId());
    }
    @Override public boolean canBlock(Permanent attacker, Permanent blocker, Ability source, Game game, boolean canUseChooseDialogs) {
        return attacker == null || !attacker.isControlledBy(source.getControllerId());
    }
}
