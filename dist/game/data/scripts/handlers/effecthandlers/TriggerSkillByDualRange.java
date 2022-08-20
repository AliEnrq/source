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

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.holders.SkillUseHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * Trigger Skill By Dual Range effect implementation.
 * @author Mobius
 */
public class TriggerSkillByDualRange extends AbstractEffect
{
	private final SkillHolder _closeSkill;
	private final SkillHolder _rangeSkill;
	private final int _distance;
	private final boolean _adjustLevel;
	
	public TriggerSkillByDualRange(StatSet params)
	{
		// Just use closeSkill and rangeSkill parameters.
		_closeSkill = new SkillHolder(params.getInt("closeSkill"), params.getInt("closeSkillLevel", 1));
		_rangeSkill = new SkillHolder(params.getInt("rangeSkill"), params.getInt("rangeSkillLevel", 1));
		_distance = params.getInt("distance", 120);
		_adjustLevel = params.getBoolean("adjustLevel", true);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if ((effected == null) || !effector.isPlayer())
		{
			return;
		}
		
		final SkillHolder skillHolder = effector.calculateDistance3D(effected) < _distance ? _closeSkill : _rangeSkill;
		final Skill triggerSkill = _adjustLevel ? SkillData.getInstance().getSkill(skillHolder.getSkillId(), skill.getLevel()) : skillHolder.getSkill();
		if (triggerSkill == null)
		{
			return;
		}
		
		final SkillUseHolder queuedSkill = effector.getActingPlayer().getQueuedSkill();
		if (queuedSkill != null)
		{
			ThreadPool.schedule(() ->
			{
				effector.getActingPlayer().setQueuedSkill(queuedSkill.getSkill(), queuedSkill.getItem(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
			}, 10);
		}
		
		effector.getActingPlayer().setQueuedSkill(triggerSkill, null, false, false);
	}
}
