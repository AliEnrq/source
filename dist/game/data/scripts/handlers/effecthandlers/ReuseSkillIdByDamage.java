/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.OnCreatureDamageReceived;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.serverpackets.SkillCoolTime;

/**
 * @author NasSeKa
 */
public class ReuseSkillIdByDamage extends AbstractEffect
{
	private final int _minAttackerLevel;
	private final int _maxAttackerLevel;
	private final int _minDamage;
	private final int _chance;
	private final int _hpPercent;
	private final int _skillId;
	private final int _amount;
	private final InstanceType _attackerType;
	
	public ReuseSkillIdByDamage(StatSet params)
	{
		_minAttackerLevel = params.getInt("minAttackerLevel", 1);
		_maxAttackerLevel = params.getInt("maxAttackerLevel", Integer.MAX_VALUE);
		_minDamage = params.getInt("minDamage", 1);
		_chance = params.getInt("chance", 100);
		_hpPercent = params.getInt("hpPercent", 100);
		_skillId = params.getInt("skillId", 0);
		_amount = params.getInt("amount", 0);
		_attackerType = params.getEnum("attackerType", InstanceType.class, InstanceType.Creature);
	}
	
	private void onDamageReceivedEvent(OnCreatureDamageReceived event)
	{
		if (event.isDamageOverTime() || (_chance == 0))
		{
			return;
		}
		
		if (event.getAttacker() == event.getTarget())
		{
			return;
		}
		
		if ((event.getAttacker().getLevel() < _minAttackerLevel) || (event.getAttacker().getLevel() > _maxAttackerLevel))
		{
			return;
		}
		
		if (event.getDamage() < _minDamage)
		{
			return;
		}
		
		if ((_chance < 100) && (Rnd.get(100) > _chance))
		{
			return;
		}
		
		if ((_hpPercent < 100) && (event.getAttacker().getCurrentHpPercent() > _hpPercent))
		{
			return;
		}
		
		if (!event.getAttacker().getInstanceType().isType(_attackerType))
		{
			return;
		}
		
		final Player player = (Player) event.getTarget();
		final Skill s = player.getKnownSkill(_skillId);
		if (s != null)
		{
			if (_amount > 0)
			{
				final long reuse = player.getSkillRemainingReuseTime(s.getReuseHashCode());
				if (reuse > 0)
				{
					player.removeTimeStamp(s);
					player.addTimeStamp(s, Math.max(0, reuse - _amount));
					player.sendPacket(new SkillCoolTime(player));
				}
			}
			else
			{
				player.removeTimeStamp(s);
				player.enableSkill(s);
				player.sendPacket(new SkillCoolTime(player));
			}
		}
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		effected.removeListenerIf(EventType.ON_CREATURE_DAMAGE_RECEIVED, listener -> listener.getOwner() == this);
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, Item item)
	{
		effected.addListener(new ConsumerEventListener(effected, EventType.ON_CREATURE_DAMAGE_RECEIVED, (OnCreatureDamageReceived event) -> onDamageReceivedEvent(event), this));
	}
}
