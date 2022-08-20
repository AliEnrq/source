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

import org.l2jmobius.gameserver.enums.BonusExpType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.serverpackets.ExUserBoostStat;

/**
 * @author Mobius
 */
public class VitalityExpRate extends AbstractStatPercentEffect
{
	public VitalityExpRate(StatSet params)
	{
		super(params, Stat.VITALITY_EXP_RATE);
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		effected.getStat().mergeMul(Stat.VITALITY_EXP_RATE, (_amount / 100) + 1);
		effected.getStat().mergeAdd(Stat.VITALITY_SKILLS, 1);

		final Player player = effected.getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		player.sendPacket(new ExUserBoostStat(player, BonusExpType.VITALITY));
		player.sendPacket(new ExUserBoostStat(player, BonusExpType.BUFFS));
		player.sendPacket(new ExUserBoostStat(player, BonusExpType.PASSIVE));
	}
}
