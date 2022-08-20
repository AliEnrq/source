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
package org.l2jmobius.gameserver.network.serverpackets.magiclamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.MagicLampData;
import org.l2jmobius.gameserver.enums.LampMode;
import org.l2jmobius.gameserver.enums.LampType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.MagicLampDataHolder;
import org.l2jmobius.gameserver.model.holders.MagicLampHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author L2CCCP
 */
public class ExMagicLampGameResult implements IClientOutgoingPacket
{
	private final Map<LampType, MagicLampHolder> _reward = new HashMap<>();
	
	public ExMagicLampGameResult(Player player, int count, byte mode)
	{
		final LampMode type = LampMode.getByMode(mode);
		final int consume = calcConsume(type, count);
		final int have = player.getLampCount();
		if (have >= consume)
		{
			init(type, count);
			player.setLampCount(have - consume);
			_reward.values().forEach(lamp -> player.addExpAndSp(lamp.getExp(), lamp.getSp()));
			// update UI
			final int left = player.getLampCount();
			player.sendPacket(new ExMagicLampGameInfoUI(player, mode, left > consume ? count : left)); // check left count for update UI
			player.sendPacket(new ExMagicLampExpInfoUI(player));
		}
	}
	
	private void init(LampMode mode, int count)
	{
		for (int x = count; x > 0; x--)
		{
			final List<MagicLampDataHolder> available = MagicLampData.getInstance().getLamps().stream().filter(lamp -> (lamp.getMode() == mode) && chance(lamp.getChance())).collect(Collectors.toList());
			final MagicLampDataHolder random = getRandom(available);
			if (random != null)
			{
				_reward.computeIfAbsent(random.getType(), list -> new MagicLampHolder(random)).inc();
			}
		}
	}
	
	private boolean chance(double chance)
	{
		return (chance > 0) && ((chance >= 100) || (Rnd.get(100d) <= chance));
	}
	
	private <E> E getRandom(List<E> list)
	{
		if (list.isEmpty())
		{
			return null;
		}
		if (list.size() == 1)
		{
			return list.get(0);
		}
		return list.get(Rnd.get(list.size()));
	}
	
	private int calcConsume(LampMode mode, int count)
	{
		switch (mode)
		{
			case NORMAL:
			{
				return Config.MAGIC_LAMP_REWARD_COUNT * count;
			}
			case GREATER:
			{
				return Config.MAGIC_LAMP_GREATER_REWARD_COUNT * count;
			}
			default:
			{
				return 0;
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_MAGICLAMP_GAME_RESULT.writeId(packet);
		packet.writeD(_reward.size()); // magicLampGameResult
		for (MagicLampHolder lamp : _reward.values())
		{
			packet.writeC(lamp.getType().getGrade()); // cGradeNum
			packet.writeD(lamp.getCount()); // nRewardCount
			packet.writeQ(lamp.getExp()); // nEXP
			packet.writeQ(lamp.getSp()); // nSP
		}
		return true;
	}
}