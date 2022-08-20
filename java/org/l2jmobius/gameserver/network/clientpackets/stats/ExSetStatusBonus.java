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
package org.l2jmobius.gameserver.network.clientpackets.stats;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * @author Mobius
 */
public class ExSetStatusBonus implements IClientIncomingPacket
{
	private int _str;
	private int _dex;
	private int _con;
	private int _int;
	private int _wit;
	private int _men;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		packet.readH(); // unk
		packet.readH(); // totalBonus
		_str = packet.readH();
		_dex = packet.readH();
		_con = packet.readH();
		_int = packet.readH();
		_wit = packet.readH();
		_men = packet.readH();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		if ((_str < 0) || (_dex < 0) || (_con < 0) || (_int < 0) || (_wit < 0) || (_men < 0))
		{
			return;
		}
		
		final int usedPoints = player.getVariables().getInt(PlayerVariables.STAT_POINTS, 0);
		final int elixirsAvailable = player.getVariables().getInt(PlayerVariables.ELIXIRS_AVAILABLE, 0);
		final int elixirsUsed = player.getVariables().getInt(PlayerVariables.ELIXIRS_USED, 0);
		final int currentPoints = _str + _dex + _con + _int + _wit + _men;
		if ((((player.getLevel() - 75) + elixirsAvailable) - usedPoints - currentPoints) < 0)
		{
			return;
		}
		
		if (((player.getLevel() - 75) - usedPoints - currentPoints) < 0)
		{
			final int neededElixirs = -((player.getLevel() - 75) - usedPoints - currentPoints);
			final int neededPoints = currentPoints - neededElixirs;
			
			player.getVariables().set(PlayerVariables.ELIXIRS_AVAILABLE, Math.min(0, elixirsAvailable - neededElixirs));
			player.getVariables().set(PlayerVariables.ELIXIRS_USED, elixirsUsed + neededElixirs);
			player.getVariables().set(PlayerVariables.STAT_POINTS, usedPoints + neededPoints);
		}
		else
		{
			player.getVariables().set(PlayerVariables.STAT_POINTS, usedPoints + currentPoints);
		}
		if (_str > 0)
		{
			player.getVariables().set(PlayerVariables.STAT_STR, player.getVariables().getInt(PlayerVariables.STAT_STR, 0) + _str);
		}
		if (_dex > 0)
		{
			player.getVariables().set(PlayerVariables.STAT_DEX, player.getVariables().getInt(PlayerVariables.STAT_DEX, 0) + _dex);
		}
		if (_con > 0)
		{
			player.getVariables().set(PlayerVariables.STAT_CON, player.getVariables().getInt(PlayerVariables.STAT_CON, 0) + _con);
		}
		if (_int > 0)
		{
			player.getVariables().set(PlayerVariables.STAT_INT, player.getVariables().getInt(PlayerVariables.STAT_INT, 0) + _int);
		}
		if (_wit > 0)
		{
			player.getVariables().set(PlayerVariables.STAT_WIT, player.getVariables().getInt(PlayerVariables.STAT_WIT, 0) + _wit);
		}
		if (_men > 0)
		{
			player.getVariables().set(PlayerVariables.STAT_MEN, player.getVariables().getInt(PlayerVariables.STAT_MEN, 0) + _men);
		}
		
		player.sendPacket(new UserInfo(player));
		
		// Calculate stat increase skills.
		player.calculateStatIncreaseSkills();
	}
}
