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

import static org.l2jmobius.gameserver.model.itemcontainer.Inventory.LCOIN_ID;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.limitshop.ExBloodyCoinCount;

/**
 * @author Mobius
 */
public class ExResetStatusBonus implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
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
		
		final int points = player.getVariables().getInt(PlayerVariables.STAT_POINTS, 0);
		int adenaCost = 5000000;
		int lcoinCost = 600;
		switch (points)
		{
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			{
				adenaCost = 200000;
				lcoinCost = 200;
				break;
			}
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			{
				adenaCost = 500000;
				lcoinCost = 300;
				break;
			}
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			{
				adenaCost = 1000000;
				lcoinCost = 400;
				break;
			}
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			{
				adenaCost = 2000000;
				lcoinCost = 500;
				break;
			}
			case 21:
			case 22:
			case 23:
			case 24:
			case 25:
			{
				adenaCost = 5000000;
				lcoinCost = 600;
				break;
			}
		}
		
		if (player.reduceAdena("ExResetStatusBonus", adenaCost, player, true) && (player.getInventory().getInventoryItemCount(LCOIN_ID, -1) >= lcoinCost))
		{
			player.getInventory().destroyItemByItemId("ExResetStatusBonus", LCOIN_ID, lcoinCost, player, true);
			player.sendPacket(new ExBloodyCoinCount(player));
			player.getVariables().remove(PlayerVariables.STAT_POINTS);
			player.getVariables().remove(PlayerVariables.STAT_STR);
			player.getVariables().remove(PlayerVariables.STAT_DEX);
			player.getVariables().remove(PlayerVariables.STAT_CON);
			player.getVariables().remove(PlayerVariables.STAT_INT);
			player.getVariables().remove(PlayerVariables.STAT_WIT);
			player.getVariables().remove(PlayerVariables.STAT_MEN);
			player.getVariables().set(PlayerVariables.ELIXIRS_AVAILABLE, player.getVariables().getInt(PlayerVariables.ELIXIRS_USED, 0) + player.getVariables().getInt(PlayerVariables.ELIXIRS_AVAILABLE, 0));
			player.getVariables().remove(PlayerVariables.ELIXIRS_USED);
			
			player.sendPacket(new UserInfo(player));
			player.getStat().recalculateStats(true);
			
			// Calculate stat increase skills.
			player.calculateStatIncreaseSkills();
		}
	}
}
