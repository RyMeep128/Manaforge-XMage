package mage.cards.f;

import java.util.*;
import java.util.stream.Collectors;
import mage.MageInt;
import mage.MageObjectReference;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.costs.mana.ManaCostsImpl;
import mage.abilities.effects.common.CopySpellForEachItCouldTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.LifelinkAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.common.FilterCreaturePermanent;
import mage.filter.predicate.Predicates;
import mage.filter.predicate.mageobject.MageObjectReferencePredicate;
import mage.filter.predicate.permanent.PermanentIdPredicate;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.game.stack.Spell;
import mage.game.stack.StackObject;
import mage.players.Player;
import mage.target.Target;
import mage.target.TargetPermanent;
import mage.util.TargetAddress;

/** Manaforge implementation using XMage's spell-copy and target machinery. */
public final class FeatherRadiantArbiter extends CardImpl {
    public FeatherRadiantArbiter(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{R}{W}{W}");
        supertype.add(SuperType.LEGENDARY);
        subtype.add(SubType.ANGEL);
        power = new MageInt(4);
        toughness = new MageInt(3);
        addAbility(FlyingAbility.getInstance());
        addAbility(LifelinkAbility.getInstance());
        addAbility(new FeatherRadiantArbiterTrigger());
    }
    private FeatherRadiantArbiter(final FeatherRadiantArbiter card) { super(card); }
    @Override public FeatherRadiantArbiter copy() { return new FeatherRadiantArbiter(this); }
}

class FeatherRadiantArbiterTrigger extends TriggeredAbilityImpl {
    FeatherRadiantArbiterTrigger() { super(Zone.BATTLEFIELD, new FeatherRadiantArbiterCopyEffect(), false); }
    private FeatherRadiantArbiterTrigger(final FeatherRadiantArbiterTrigger ability) { super(ability); }
    @Override public FeatherRadiantArbiterTrigger copy() { return new FeatherRadiantArbiterTrigger(this); }
    @Override public boolean checkEventType(GameEvent event, Game game) { return event.getType() == GameEvent.EventType.SPELL_CAST; }
    @Override public boolean checkTrigger(GameEvent event, Game game) {
        if (!isControlledBy(event.getPlayerId())) return false;
        Spell spell = game.getSpell(event.getTargetId());
        if (spell == null || spell.isCreature(game)) return false;
        boolean targeted = false;
        for (TargetAddress address : TargetAddress.walk(spell)) {
            if (address == null) continue;
            Target target = address.getTarget(spell);
            if (target == null) continue;
            for (UUID id : target.getTargets()) {
                if (id == null) continue;
                if (!id.equals(getSourceId())) return false;
                targeted = true;
            }
        }
        if (targeted) getEffects().setValue("triggeringSpell", spell);
        return targeted;
    }
    @Override public String getRule() {
        return "Whenever you cast a noncreature spell that targets only {this}, you may choose any number of other creatures that spell could target and pay {2} for each of those creatures. If you do, for each of those creatures, copy that spell. The copy targets that creature. <i>(Copies of permanent spells become tokens.)</i>";
    }
}

class FeatherRadiantArbiterCopyEffect extends CopySpellForEachItCouldTargetEffect {
    FeatherRadiantArbiterCopyEffect() { super(); }
    private FeatherRadiantArbiterCopyEffect(final FeatherRadiantArbiterCopyEffect effect) { super(effect); }
    @Override public FeatherRadiantArbiterCopyEffect copy() { return new FeatherRadiantArbiterCopyEffect(this); }
    @Override protected Player getPlayer(Game game, Ability source) { return game.getPlayer(source.getControllerId()); }
    @Override protected StackObject getStackObject(Game game, Ability source) { return (Spell) getValue("triggeringSpell"); }
    @Override protected List<MageObjectReferencePredicate> prepareCopiesWithTargets(StackObject spell, Player player, Ability source, Game game) {
        Permanent feather = source.getSourcePermanentIfItStillExists(game);
        List<PermanentIdPredicate> candidates = game.getBattlefield().getAllActivePermanents().stream()
                .filter(p -> p.isCreature(game) && (feather == null || !p.getId().equals(feather.getId())))
                .filter(p -> spell.canTarget(game, p.getId()))
                .map(p -> new PermanentIdPredicate(p.getId())).collect(Collectors.toList());
        if (candidates.isEmpty()) return Collections.emptyList();
        FilterCreaturePermanent filter = new FilterCreaturePermanent("other creatures the spell could target");
        filter.add(Predicates.or(candidates));
        TargetPermanent choice = new TargetPermanent(0, candidates.size(), filter, true);
        player.choose(Outcome.Copy, choice, source, game);
        int count = choice.getTargets().size();
        if (count == 0) return Collections.emptyList();
        ManaCostsImpl<?> payment = new ManaCostsImpl<>("{" + (2 * count) + "}");
        if (!player.chooseUse(Outcome.Copy, "Pay " + payment.getText() + " to copy the spell for the chosen creatures?", source, game)
                || !payment.pay(source, game, source, player.getId(), false, null)) return Collections.emptyList();
        List<MageObjectReferencePredicate> result = new ArrayList<>();
        for (UUID id : choice.getTargets()) {
            Permanent permanent = game.getPermanent(id);
            if (permanent != null) result.add(new MageObjectReferencePredicate(new MageObjectReference(permanent, game)));
        }
        return result;
    }
}
