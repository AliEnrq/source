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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.BonusExpType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Mobius
 */
public class ExUserBoostStat implements IClientOutgoingPacket
{
	private final Player _player;
	private final BonusExpType _type;
	
	public ExUserBoostStat(Player player, BonusExpType type)
	{
		_player = player;
		_type = type;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_USER_BOOST_STAT.writeId(packet);
		int count = 0;
		int bonus = 0;
		switch (_type)
		{
			case VITALITY:
			{
				final int vitalityBonus = (int) (_player.getStat().getVitalityExpBonus() * 100);
				if (vitalityBonus > 0)
				{
					count = 1;
					bonus = vitalityBonus;
				}
				break;
			}
			case BUFFS:
			{
				count = (int) _player.getStat().getValue(Stat.BONUS_EXP_BUFFS, 0);
				bonus = (int) _player.getStat().getValue(Stat.ACTIVE_BONUS_EXP, 0);
				break;
			}
			case PASSIVE:
			{
				count = (int) _player.getStat().getValue(Stat.BONUS_EXP_PASSIVES, 0);
				bonus = (int) (_player.getStat().getValue(Stat.BONUS_EXP, 0) - _player.getStat().getValue(Stat.ACTIVE_BONUS_EXP, 0));
				break;
			}
		}
		packet.writeC(_type.getId());
		packet.writeC(count);
		packet.writeH(bonus);
		return true;
	}
}
