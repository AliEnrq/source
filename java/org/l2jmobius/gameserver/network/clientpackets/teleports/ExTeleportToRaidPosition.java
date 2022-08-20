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
package org.l2jmobius.gameserver.network.clientpackets.teleports;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.data.xml.RaidTeleportListData;
import org.l2jmobius.gameserver.enums.RaidBossStatus;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.DBSpawnManager;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.effects.EffectFlag;
import org.l2jmobius.gameserver.model.holders.TeleportListHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.skill.CommonSkill;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.teleports.ExRaidTeleportInfo;

/**
 * @author Gustavo Fonseca
 */
public class ExTeleportToRaidPosition implements IClientIncomingPacket
{
	private int _raidId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_raidId = packet.readD();
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
		
		final TeleportListHolder teleport = RaidTeleportListData.getInstance().getTeleport(_raidId);
		if (teleport == null)
		{
			PacketLogger.warning("No registered teleport location for raid id: " + _raidId);
			return;
		}
		
		// Dead characters cannot use teleports.
		if (player.isDead())
		{
			player.sendPacket(SystemMessageId.DEAD_CHARACTERS_CANNOT_USE_TELEPORTS);
			return;
		}
		
		final NpcTemplate template = NpcData.getInstance().getTemplate(_raidId);
		if (template.isType("GrandBoss") && (GrandBossManager.getInstance().getBossStatus(_raidId) != 0))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			return;
		}
		else if (template.isType("RaidBoss") && (DBSpawnManager.getInstance().getNpcStatusId(_raidId) != RaidBossStatus.ALIVE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			return;
		}
		
		// Players should not be able to teleport if in combat, or in a special location.
		if (player.isCastingNow() || player.isInCombat() || player.isImmobilized() || player.isInInstance() || player.isOnEvent() || player.isInOlympiadMode() || player.inObserverMode() || player.isInTraingCamp() || player.isInsideZone(ZoneId.TIMED_HUNTING))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			return;
		}
		
		// Karma related configurations.
		if ((!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT || !Config.ALT_GAME_KARMA_PLAYER_CAN_USE_GK) && (player.getReputation() < 0))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			return;
		}
		
		// Cannot escape effect.
		if (player.isAffected(EffectFlag.CANNOT_ESCAPE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_RIGHT_NOW);
			return;
		}
		
		if (!Config.TELEPORT_WHILE_SIEGE_IN_PROGRESS)
		{
			final Castle castle = CastleManager.getInstance().getCastle(teleport.getX(), teleport.getY(), teleport.getZ());
			if ((castle != null) && castle.getSiege().isInProgress())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
		}
		
		final int price;
		if ((System.currentTimeMillis() - player.getVariables().getLong("LastFreeRaidTeleportTime", 0)) > 86400000)
		{
			player.getVariables().set("LastFreeRaidTeleportTime", System.currentTimeMillis());
			price = 0;
		}
		else
		{
			price = teleport.getPrice();
		}
		
		if (price > 0)
		{
			if (player.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < price)
			{
				player.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
				return;
			}
			player.destroyItemByItemId("TeleportToRaid", Inventory.LCOIN_ID, price, player, true);
		}
		
		player.abortCast();
		player.stopMove(null);
		
		player.setTeleportLocation(new Location(teleport.getX(), teleport.getY(), teleport.getZ()));
		player.doCast(CommonSkill.TELEPORT.getSkill());
		player.sendPacket(new ExRaidTeleportInfo());
	}
}