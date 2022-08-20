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
package handlers.skillconditionhandlers;

import org.l2jmobius.gameserver.enums.SkillConditionAffectType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.ISkillCondition;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * @author Mobius
 */
public class OpEquipItemSkillCondition implements ISkillCondition
{
	private final int _itemId;
	private final SkillConditionAffectType _affectType;
	
	public OpEquipItemSkillCondition(StatSet params)
	{
		_itemId = params.getInt("itemId");
		_affectType = params.getEnum("affectType", SkillConditionAffectType.class);
	}
	
	@Override
	public boolean canUse(Creature caster, Skill skill, WorldObject target)
	{
		switch (_affectType)
		{
			case CASTER:
			{
				for (Item item : caster.getInventory().getPaperdollItems())
				{
					if (item.getId() == _itemId)
					{
						return true;
					}
				}
				return false;
			}
			case TARGET:
			{
				if ((target != null) && target.isPlayer())
				{
					for (Item item : target.getActingPlayer().getInventory().getPaperdollItems())
					{
						if (item.getId() == _itemId)
						{
							return true;
						}
					}
				}
				return false;
			}
			case BOTH:
			{
				if ((target != null) && target.isPlayer())
				{
					for (Item item : caster.getInventory().getPaperdollItems())
					{
						if (item.getId() == _itemId)
						{
							for (Item i : target.getActingPlayer().getInventory().getPaperdollItems())
							{
								if (i.getId() == _itemId)
								{
									return true;
								}
							}
							return false;
						}
					}
				}
				return false;
			}
		}
		return false;
	}
}
