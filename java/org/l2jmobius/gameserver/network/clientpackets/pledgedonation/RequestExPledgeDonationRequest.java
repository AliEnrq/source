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
package org.l2jmobius.gameserver.network.clientpackets.pledgedonation;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.enums.MailType;
import org.l2jmobius.gameserver.instancemanager.MailManager;
import org.l2jmobius.gameserver.model.Message;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.itemcontainer.Mail;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.limitshop.ExBloodyCoinCount;
import org.l2jmobius.gameserver.network.serverpackets.pledgedonation.ExPledgeDonationRequest;

/**
 * Written by Berezkin Nikolay, on 08.05.2021
 */
public class RequestExPledgeDonationRequest implements IClientIncomingPacket
{
	private int _type;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_type = packet.readC();
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
		
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		
		switch (_type)
		{
			case 0:
			{
				if (player.reduceAdena("pledge donation", 10000, null, true))
				{
					clan.addExp(player.getObjectId(), 9);
				}
				else
				{
					player.sendPacket(new ExPledgeDonationRequest(false, _type, 2));
				}
				break;
			}
			case 1:
			{
				if (player.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) >= 100)
				{
					if (player.getInventory().destroyItemByItemId("pledge donation", Inventory.LCOIN_ID, 100, player, null) != null)
					{
						clan.addExp(player.getObjectId(), 30);
						player.setHonorCoins(player.getHonorCoins() + 100);
					}
					else
					{
						player.sendPacket(new ExPledgeDonationRequest(false, _type, 2));
					}
				}
				else
				{
					player.sendPacket(new ExPledgeDonationRequest(false, _type, 2));
				}
				break;
			}
			case 2:
			{
				if (player.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) >= 500)
				{
					if (player.getInventory().destroyItemByItemId("pledge donation", Inventory.LCOIN_ID, 500, player, null) != null)
					{
						clan.addExp(player.getObjectId(), 150);
						player.setHonorCoins(player.getHonorCoins() + 500);
					}
					else
					{
						player.sendPacket(new ExPledgeDonationRequest(false, _type, 2));
					}
				}
				else
				{
					player.sendPacket(new ExPledgeDonationRequest(false, _type, 2));
				}
				break;
			}
		}
		player.setClanDonationPoints(Math.max(player.getClanDonationPoints() - 1, 0));
		criticalSuccess(player, clan, _type);
		player.sendPacket(new ExBloodyCoinCount(player));
		player.sendItemList();
		player.sendPacket(new ExPledgeDonationRequest(true, _type, player.getClanDonationPoints()));
	}
	
	private void criticalSuccess(Player player, Clan clan, int type)
	{
		if (type == 1)
		{
			if (Rnd.get(100) < 10)
			{
				player.setHonorCoins(player.getHonorCoins() + 200);
				clan.getMembers().forEach(clanMember ->
				{
					sendMail(clanMember.getObjectId(), 1, player.getName());
				});
			}
		}
		else if (type == 2)
		{
			if (Rnd.get(100) < 5)
			{
				player.setHonorCoins(player.getHonorCoins() + 1000);
				clan.getMembers().forEach(clanMember ->
				{
					sendMail(clanMember.getObjectId(), 5, player.getName());
				});
			}
		}
	}
	
	private void sendMail(int charId, int amount, String donator)
	{
		final Message msg = new Message(charId, "Clan Rewards for " + donator + " Donation", "The entire clan receives rewards for " + donator + " donation.", MailType.PLEDGE_DONATION_CRITICAL_SUCCESS);
		final Mail attachment = msg.createAttachments();
		attachment.addItem("Pledge reward", 95672, amount, null, null);
		MailManager.getInstance().sendMessage(msg);
	}
}
