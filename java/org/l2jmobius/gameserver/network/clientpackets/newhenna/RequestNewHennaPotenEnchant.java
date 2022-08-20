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
package org.l2jmobius.gameserver.network.clientpackets.newhenna;

import java.util.Map.Entry;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.HennaPatternPotentialData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.henna.DyePotentialFee;
import org.l2jmobius.gameserver.model.item.henna.HennaPoten;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.newhenna.NewHennaPotenEnchant;

/**
 * @author Index, Serenitty
 */
public class RequestNewHennaPotenEnchant implements IClientIncomingPacket
{
	private int _slotId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_slotId = packet.readC();
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
		
		if ((_slotId < 1) || (_slotId > 4))
		{
			return;
		}
		
		int dailyStep = player.getDyePotentialDailyStep();
		final DyePotentialFee currentFee = HennaPatternPotentialData.getInstance().getFee(dailyStep);
		int dailyCount = player.getDyePotentialDailyCount();
		if ((currentFee == null) || (dailyCount <= 0))
		{
			return;
		}
		
		if (!player.destroyItemByItemId(getClass().getSimpleName(), currentFee.getItem().getId(), currentFee.getItem().getCount(), player, true))
		{
			return;
		}
		dailyCount -= 1;
		if ((dailyCount <= 0) && (dailyStep != HennaPatternPotentialData.getInstance().getMaxPotenEnchantStep()))
		{
			dailyStep += 1;
			final DyePotentialFee newFee = HennaPatternPotentialData.getInstance().getFee(dailyStep);
			if (newFee != null)
			{
				dailyCount = newFee.getDailyCount();
			}
			player.setDyePotentialDailyCount(dailyCount);
			player.setDyePotentialDailyStep(dailyStep);
		}
		else
		{
			player.setDyePotentialDailyCount(dailyCount);
		}
		double totalChance = 0;
		double random = Rnd.nextDouble() * 100;
		for (Entry<Integer, Double> entry : currentFee.getEnchantExp().entrySet())
		{
			totalChance += entry.getValue();
			if (random <= totalChance)
			{
				final HennaPoten poten = player.getHennaPoten(_slotId);
				final int increase = entry.getKey();
				int newEnchantExp = poten.getEnchantExp() + increase;
				final int tatooExpNeeded = HennaPatternPotentialData.getInstance().getExpForLevel(poten.getEnchantLevel());
				if (newEnchantExp >= tatooExpNeeded)
				{
					newEnchantExp -= tatooExpNeeded;
					if (poten.getEnchantLevel() < HennaPatternPotentialData.getInstance().getMaxPotenLevel())
					{
						poten.setEnchantLevel(poten.getEnchantLevel() + 1);
						player.applyDyePotenSkills();
					}
				}
				poten.setEnchantExp(newEnchantExp);
				player.sendPacket(new NewHennaPotenEnchant(_slotId, poten.getEnchantLevel(), poten.getEnchantExp(), dailyStep, dailyCount, poten.getActiveStep(), true));
				return;
			}
		}
	}
}
