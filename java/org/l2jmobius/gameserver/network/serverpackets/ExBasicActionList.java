Index: dist/game/data/scripts/handlers/chathandlers/ChatPetition.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatPetition.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatPetition.java	(working copy)
@@ -37,7 +37,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
 		{
Index: java/org/l2jmobius/gameserver/network/serverpackets/ExBasicActionList.java
===================================================================
--- java/org/l2jmobius/gameserver/network/serverpackets/ExBasicActionList.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/network/serverpackets/ExBasicActionList.java	(working copy)
@@ -100,7 +100,7 @@
 		81, 82, 83, 84,
 		85, 86, 87, 88,
 		89, 90, 92, 93,
-		94, 96, 97,
+		94, 96, 97, 99,
 		1000, 1001,
 		1002, 1003, 1004, 1005,
 		1006, 1007, 1008, 1009,
Index: dist/game/data/scripts/handlers/chathandlers/ChatPartyRoomCommander.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatPartyRoomCommander.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatPartyRoomCommander.java	(working copy)
@@ -36,7 +36,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (activeChar.isInParty() && activeChar.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getLeader().equals(activeChar))
 		{
@@ -50,7 +50,7 @@
 				activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
 				return;
 			}
-			activeChar.getParty().getCommandChannel().broadcastCreatureSay(new CreatureSay(activeChar, type, activeChar.getName(), text), activeChar);
+			activeChar.getParty().getCommandChannel().broadcastCreatureSay(new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing), activeChar);
 		}
 	}
 	
Index: java/org/l2jmobius/gameserver/network/serverpackets/teleports/ExShowSharedLocationTeleportUi.java
===================================================================
--- java/org/l2jmobius/gameserver/network/serverpackets/teleports/ExShowSharedLocationTeleportUi.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/network/serverpackets/teleports/ExShowSharedLocationTeleportUi.java	(working copy)
@@ -17,18 +17,20 @@
 package org.l2jmobius.gameserver.network.serverpackets.teleports;
 
 import org.l2jmobius.commons.network.PacketWriter;
+import org.l2jmobius.gameserver.instancemanager.SharedTeleportManager;
 import org.l2jmobius.gameserver.network.OutgoingPackets;
 import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
 
 /**
- * @author Gustavo Fonseca
+ * @author NasSeKa
  */
 public class ExShowSharedLocationTeleportUi implements IClientOutgoingPacket
 {
-	public static final ExShowSharedLocationTeleportUi STATIC_PACKET = new ExShowSharedLocationTeleportUi();
+	private static int _id;
 	
-	public ExShowSharedLocationTeleportUi()
+	public ExShowSharedLocationTeleportUi(int id)
 	{
+		_id = id;
 	}
 	
 	@Override
@@ -35,6 +37,15 @@
 	public boolean write(PacketWriter packet)
 	{
 		OutgoingPackets.EX_SHARED_POSITION_TELEPORT_UI.writeId(packet);
+		
+		packet.writeString(SharedTeleportManager.getInstance().getCharName(_id)); // Name 2
+		packet.writeD(_id);
+		packet.writeD(SharedTeleportManager.getInstance().getTimesUsed(_id));
+		packet.writeH(150);
+		packet.writeD(SharedTeleportManager.getInstance().getX(_id));
+		packet.writeD(SharedTeleportManager.getInstance().getY(_id));
+		packet.writeD(SharedTeleportManager.getInstance().getZ(_id));
+		
 		return true;
 	}
 }
\ No newline at end of file
Index: dist/game/data/ActionData.xml
===================================================================
--- dist/game/data/ActionData.xml	(revision 10469)
+++ dist/game/data/ActionData.xml	(working copy)
@@ -63,6 +63,7 @@
 	<action id="88" handler="SocialAction" option="29" /> <!-- Provoke -->
 	<action id="89" handler="SocialAction" option="30" /> <!-- Beauty Shop -->
 	<action id="90" handler="InstanceZoneInfo" />
+	<action id="99" handler="SharedPosition" />
 	<action id="1000" handler="ServitorSkillUse" option="4079" /> <!-- Siege Golem - Siege Hammer -->
 	<action id="1003" handler="PetSkillUse" option="4710" /> <!-- Wind Hatchling/Strider - Wild Stun -->
 	<action id="1004" handler="PetSkillUse" option="4711" /> <!-- Wind Hatchling/Strider - Wild Defense -->
Index: dist/game/data/scripts/handlers/chathandlers/ChatAlliance.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatAlliance.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatAlliance.java	(working copy)
@@ -21,6 +21,8 @@
 import org.l2jmobius.gameserver.enums.PlayerCondOverride;
 import org.l2jmobius.gameserver.handler.IChatHandler;
 import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
+import org.l2jmobius.gameserver.model.zone.ZoneId;
 import org.l2jmobius.gameserver.network.SystemMessageId;
 import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
 
@@ -35,7 +37,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if ((activeChar.getClan() == null) || ((activeChar.getClan() != null) && (activeChar.getClan().getAllyId() == 0)))
 		{
@@ -53,7 +55,25 @@
 			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
 			return;
 		}
-		activeChar.getClan().broadcastToOnlineAllyMembers(new CreatureSay(activeChar, type, activeChar.getName(), text));
+		
+		if ((isLocSharing == 1) && (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.SHARING_LOCATION_COST))
+		{
+			activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
+			return;
+		}
+		
+		if ((isLocSharing == 1) && (activeChar.isInInstance() || activeChar.isInsideZone(ZoneId.SIEGE)))
+		{
+			activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
+			return;
+		}
+		
+		if (isLocSharing == 1)
+		{
+			activeChar.destroyItemByItemId("TeleToSharedLoc", Inventory.LCOIN_ID, Config.SHARING_LOCATION_COST, activeChar, true);
+		}
+		
+		activeChar.getClan().broadcastToOnlineAllyMembers(new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing));
 	}
 	
 	@Override
Index: java/org/l2jmobius/gameserver/network/serverpackets/CreatureSay.java
===================================================================
--- java/org/l2jmobius/gameserver/network/serverpackets/CreatureSay.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/network/serverpackets/CreatureSay.java	(working copy)
@@ -23,6 +23,7 @@
 import org.l2jmobius.gameserver.enums.ChatType;
 import org.l2jmobius.gameserver.instancemanager.MentorManager;
 import org.l2jmobius.gameserver.instancemanager.RankManager;
+import org.l2jmobius.gameserver.instancemanager.SharedTeleportManager;
 import org.l2jmobius.gameserver.model.actor.Creature;
 import org.l2jmobius.gameserver.model.actor.Player;
 import org.l2jmobius.gameserver.model.clan.Clan;
@@ -40,6 +41,7 @@
 	private int _messageId = -1;
 	private int _mask;
 	private List<String> _parameters;
+	private int _isLocSharing;
 	
 	/**
 	 * @param sender
@@ -47,13 +49,15 @@
 	 * @param name
 	 * @param chatType
 	 * @param text
+	 * @param isLocSharing
 	 */
-	public CreatureSay(Player sender, Player receiver, String name, ChatType chatType, String text)
+	public CreatureSay(Player sender, Player receiver, String name, ChatType chatType, String text, int isLocSharing)
 	{
 		_sender = sender;
 		_senderName = name;
 		_chatType = chatType;
 		_text = text;
+		_isLocSharing = isLocSharing;
 		if (receiver != null)
 		{
 			if (receiver.getFriendList().contains(sender.getObjectId()))
@@ -80,12 +84,13 @@
 		}
 	}
 	
-	public CreatureSay(Creature sender, ChatType chatType, String senderName, String text)
+	public CreatureSay(Creature sender, ChatType chatType, String senderName, String text, int isLocSharing)
 	{
 		_sender = sender;
 		_chatType = chatType;
 		_senderName = senderName;
 		_text = text;
+		_isLocSharing = isLocSharing;
 	}
 	
 	public CreatureSay(Creature sender, ChatType chatType, NpcStringId npcStringId)
@@ -187,6 +192,12 @@
 			{
 				packet.writeC(0);
 			}
+			if ((_isLocSharing == 1))
+			{
+				packet.writeC(1);
+				SharedTeleportManager.getInstance().addSharedTeleport((Player) _sender);
+				packet.writeH(SharedTeleportManager.getInstance().getLastSharedId());
+			}
 		}
 		else
 		{
Index: dist/game/data/scripts/handlers/chathandlers/ChatPartyMatchRoom.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatPartyMatchRoom.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatPartyMatchRoom.java	(working copy)
@@ -37,7 +37,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		final MatchingRoom room = activeChar.getMatchingRoom();
 		if (room != null)
@@ -53,7 +53,7 @@
 				return;
 			}
 			
-			final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);
+			final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing);
 			for (Player _member : room.getMembers())
 			{
 				if (Config.FACTION_SYSTEM_ENABLED)
Index: java/org/l2jmobius/gameserver/data/sql/AnnouncementsTable.java
===================================================================
--- java/org/l2jmobius/gameserver/data/sql/AnnouncementsTable.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/data/sql/AnnouncementsTable.java	(working copy)
@@ -110,7 +110,7 @@
 		{
 			if (announce.isValid() && (announce.getType() == type))
 			{
-				player.sendPacket(new CreatureSay(null, type == AnnouncementType.CRITICAL ? ChatType.CRITICAL_ANNOUNCE : ChatType.ANNOUNCEMENT, player.getName(), announce.getContent()));
+				player.sendPacket(new CreatureSay(null, type == AnnouncementType.CRITICAL ? ChatType.CRITICAL_ANNOUNCE : ChatType.ANNOUNCEMENT, player.getName(), announce.getContent(), 0));
 			}
 		}
 	}
Index: dist/game/data/scripts/events/WatermelonNinja/WatermelonNinja.java
===================================================================
--- dist/game/data/scripts/events/WatermelonNinja/WatermelonNinja.java	(revision 10469)
+++ dist/game/data/scripts/events/WatermelonNinja/WatermelonNinja.java	(working copy)
@@ -611,7 +611,7 @@
 	{
 		if (getRandom(100) < 20)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _CHRONO_TEXT[getRandom(_CHRONO_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _CHRONO_TEXT[getRandom(_CHRONO_TEXT.length)], 0));
 		}
 	}
 	
@@ -619,7 +619,7 @@
 	{
 		if (getRandom(100) < 20)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NOCHRONO_TEXT[getRandom(_NOCHRONO_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NOCHRONO_TEXT[getRandom(_NOCHRONO_TEXT.length)], 0));
 		}
 	}
 	
@@ -627,7 +627,7 @@
 	{
 		if (getRandom(100) < 30)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NECTAR_TEXT[getRandom(_NECTAR_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NECTAR_TEXT[getRandom(_NECTAR_TEXT.length)], 0));
 		}
 	}
 	
Index: dist/game/data/scripts/handlers/admincommandhandlers/AdminTargetSay.java
===================================================================
--- dist/game/data/scripts/handlers/admincommandhandlers/AdminTargetSay.java	(revision 10469)
+++ dist/game/data/scripts/handlers/admincommandhandlers/AdminTargetSay.java	(working copy)
@@ -53,7 +53,7 @@
 				
 				final String message = command.substring(16);
 				final Creature target = (Creature) obj;
-				target.broadcastPacket(new CreatureSay(target, target.isPlayer() ? ChatType.GENERAL : ChatType.NPC_GENERAL, target.getName(), message));
+				target.broadcastPacket(new CreatureSay(target, target.isPlayer() ? ChatType.GENERAL : ChatType.NPC_GENERAL, target.getName(), message, 0));
 			}
 			catch (StringIndexOutOfBoundsException e)
 			{
Index: java/org/l2jmobius/gameserver/util/BuilderUtil.java
===================================================================
--- java/org/l2jmobius/gameserver/util/BuilderUtil.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/util/BuilderUtil.java	(working copy)
@@ -42,7 +42,7 @@
 	{
 		if (Config.GM_STARTUP_BUILDER_HIDE)
 		{
-			player.sendPacket(new CreatureSay(null, ChatType.GENERAL, "SYS", SendMessageLocalisationData.getLocalisation(player, message)));
+			player.sendPacket(new CreatureSay(null, ChatType.GENERAL, "SYS", SendMessageLocalisationData.getLocalisation(player, message), 0));
 		}
 		else
 		{
@@ -57,7 +57,7 @@
 	 */
 	public static void sendHtmlMessage(Player player, String message)
 	{
-		player.sendPacket(new CreatureSay(null, ChatType.GENERAL, "HTML", message));
+		player.sendPacket(new CreatureSay(null, ChatType.GENERAL, "HTML", message, 0));
 	}
 	
 	/**
Index: dist/game/data/scripts/handlers/chathandlers/ChatWorld.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatWorld.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatWorld.java	(working copy)
@@ -27,6 +27,8 @@
 import org.l2jmobius.gameserver.handler.IChatHandler;
 import org.l2jmobius.gameserver.model.World;
 import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
+import org.l2jmobius.gameserver.model.zone.ZoneId;
 import org.l2jmobius.gameserver.network.SystemMessageId;
 import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
 import org.l2jmobius.gameserver.network.serverpackets.ExWorldChatCnt;
@@ -46,7 +48,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (!Config.ENABLE_WORLD_CHAT)
 		{
@@ -77,6 +79,14 @@
 		{
 			activeChar.sendPacket(SystemMessageId.YOU_HAVE_SPENT_YOUR_WORLD_CHAT_QUOTA_FOR_THE_DAY_IT_IS_RESET_DAILY_AT_7_A_M);
 		}
+		else if ((isLocSharing == 1) && (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.SHARING_LOCATION_COST))
+		{
+			activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
+		}
+		else if ((isLocSharing == 1) && (activeChar.isInInstance() || activeChar.isInsideZone(ZoneId.SIEGE)))
+		{
+			activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
+		}
 		else
 		{
 			// Verify if player is not spaming.
@@ -93,7 +103,12 @@
 				}
 			}
 			
-			final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);
+			if (isLocSharing == 1)
+			{
+				activeChar.destroyItemByItemId("TeleToSharedLoc", Inventory.LCOIN_ID, Config.SHARING_LOCATION_COST, activeChar, true);
+			}
+			
+			final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing);
 			if (Config.FACTION_SYSTEM_ENABLED && Config.FACTION_SPECIFIC_CHAT)
 			{
 				if (activeChar.isGood())
Index: java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharedLocationTeleport.java
===================================================================
--- java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharedLocationTeleport.java	(nonexistent)
+++ java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharedLocationTeleport.java	(working copy)
@@ -0,0 +1,61 @@
+/*
+ * This file is part of the L2J Mobius project.
+ * 
+ * This program is free software: you can redistribute it and/or modify
+ * it under the terms of the GNU General Public License as published by
+ * the Free Software Foundation, either version 3 of the License, or
+ * (at your option) any later version.
+ * 
+ * This program is distributed in the hope that it will be useful,
+ * but WITHOUT ANY WARRANTY; without even the implied warranty of
+ * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
+ * General Public License for more details.
+ * 
+ * You should have received a copy of the GNU General Public License
+ * along with this program. If not, see <http://www.gnu.org/licenses/>.
+ */
+package org.l2jmobius.gameserver.network.clientpackets.teleports;
+
+import org.l2jmobius.Config;
+import org.l2jmobius.commons.network.PacketReader;
+import org.l2jmobius.gameserver.instancemanager.SharedTeleportManager;
+import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
+import org.l2jmobius.gameserver.network.GameClient;
+import org.l2jmobius.gameserver.network.SystemMessageId;
+import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
+
+/**
+ * @author NasSeKa
+ */
+public class ExRequestSharedLocationTeleport implements IClientIncomingPacket
+{
+	private int _id;
+	
+	@Override
+	public boolean read(GameClient client, PacketReader packet)
+	{
+		_id = packet.readD();
+		return true;
+	}
+	
+	@Override
+	public void run(GameClient client)
+	{
+		final Player player = client.getPlayer();
+		int tpId = (_id - 1) / 256;
+		if ((player == null) || (tpId == 0) || (SharedTeleportManager.getInstance().getTimesUsed(tpId) == 0))
+		{
+			return;
+		}
+		
+		if (player.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.TELEPORT_SHARE_LOCATION_COST)
+		{
+			player.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
+			return;
+		}
+		player.destroyItemByItemId("TeleToSharedLoc", Inventory.LCOIN_ID, Config.TELEPORT_SHARE_LOCATION_COST, player, true);
+		
+		SharedTeleportManager.getInstance().teleportToSharedLocation(player, tpId);
+	}
+}
Index: java/org/l2jmobius/gameserver/model/actor/Attackable.java
===================================================================
--- java/org/l2jmobius/gameserver/model/actor/Attackable.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/model/actor/Attackable.java	(working copy)
@@ -222,7 +222,7 @@
 							_commandChannelTimer = new CommandChannelTimer(this);
 							_commandChannelLastAttack = System.currentTimeMillis();
 							ThreadPool.schedule(_commandChannelTimer, 10000); // check for last attack
-							_firstCommandChannelAttacked.broadcastPacket(new CreatureSay(null, ChatType.PARTYROOM_ALL, "", "You have looting rights!")); // TODO: retail msg
+							_firstCommandChannelAttacked.broadcastPacket(new CreatureSay(null, ChatType.PARTYROOM_ALL, "", "You have looting rights!", 0)); // TODO: retail msg
 						}
 					}
 				}
Index: dist/game/data/scripts/handlers/telnethandlers/chat/GMChat.java
===================================================================
--- dist/game/data/scripts/handlers/telnethandlers/chat/GMChat.java	(revision 10469)
+++ dist/game/data/scripts/handlers/telnethandlers/chat/GMChat.java	(working copy)
@@ -52,7 +52,7 @@
 		{
 			sb.append(str + " ");
 		}
-		AdminData.getInstance().broadcastToGMs(new CreatureSay(null, ChatType.ALLIANCE, "Telnet GM Broadcast", sb.toString()));
+		AdminData.getInstance().broadcastToGMs(new CreatureSay(null, ChatType.ALLIANCE, "Telnet GM Broadcast", sb.toString(), 0));
 		return "GMChat sent!";
 	}
 }
Index: dist/game/data/scripts/events/MerrySquashmas/MerrySquashmas.java
===================================================================
--- dist/game/data/scripts/events/MerrySquashmas/MerrySquashmas.java	(revision 10469)
+++ dist/game/data/scripts/events/MerrySquashmas/MerrySquashmas.java	(working copy)
@@ -624,7 +624,7 @@
 	{
 		if (getRandom(100) < 20)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _CHRONO_TEXT[getRandom(_CHRONO_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _CHRONO_TEXT[getRandom(_CHRONO_TEXT.length)], 0));
 		}
 	}
 	
@@ -632,7 +632,7 @@
 	{
 		if (getRandom(100) < 20)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NOCHRONO_TEXT[getRandom(_NOCHRONO_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NOCHRONO_TEXT[getRandom(_NOCHRONO_TEXT.length)], 0));
 		}
 	}
 	
@@ -640,7 +640,7 @@
 	{
 		if (getRandom(100) < 30)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NECTAR_TEXT[getRandom(_NECTAR_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NECTAR_TEXT[getRandom(_NECTAR_TEXT.length)], 0));
 		}
 	}
 	
Index: dist/game/data/scripts/handlers/chathandlers/ChatPartyRoomAll.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatPartyRoomAll.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatPartyRoomAll.java	(working copy)
@@ -36,7 +36,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (activeChar.isInParty() && activeChar.getParty().isInCommandChannel() && activeChar.getParty().isLeader(activeChar))
 		{
@@ -50,7 +50,7 @@
 				activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
 				return;
 			}
-			activeChar.getParty().getCommandChannel().broadcastCreatureSay(new CreatureSay(activeChar, type, activeChar.getName(), text), activeChar);
+			activeChar.getParty().getCommandChannel().broadcastCreatureSay(new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing), activeChar);
 		}
 	}
 	
Index: dist/game/config/General.ini
===================================================================
--- dist/game/config/General.ini	(revision 10469)
+++ dist/game/config/General.ini	(working copy)
@@ -688,7 +688,7 @@
 
 Share loction L-Coin cost.
 # Default: 50
-ShareLocationLcoinCost = 1000
+ShareLocationLcoinCost = 50
 
 # Teleport share location L-Coin cost.
 # Default: 400
Index: dist/game/data/scripts/events/SquashEvent/SquashEvent.java
===================================================================
--- dist/game/data/scripts/events/SquashEvent/SquashEvent.java	(revision 10469)
+++ dist/game/data/scripts/events/SquashEvent/SquashEvent.java	(working copy)
@@ -623,7 +623,7 @@
 	{
 		if (getRandom(100) < 20)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _CHRONO_TEXT[getRandom(_CHRONO_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _CHRONO_TEXT[getRandom(_CHRONO_TEXT.length)], 0));
 		}
 	}
 	
@@ -631,7 +631,7 @@
 	{
 		if (getRandom(100) < 20)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NOCHRONO_TEXT[getRandom(_NOCHRONO_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NOCHRONO_TEXT[getRandom(_NOCHRONO_TEXT.length)], 0));
 		}
 	}
 	
@@ -639,7 +639,7 @@
 	{
 		if (getRandom(100) < 30)
 		{
-			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NECTAR_TEXT[getRandom(_NECTAR_TEXT.length)]));
+			npc.broadcastPacket(new CreatureSay(npc, ChatType.NPC_GENERAL, npc.getName(), _NECTAR_TEXT[getRandom(_NECTAR_TEXT.length)], 0));
 		}
 	}
 	
Index: dist/game/data/scripts/handlers/chathandlers/ChatHeroVoice.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatHeroVoice.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatHeroVoice.java	(working copy)
@@ -38,7 +38,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (!activeChar.isHero() && !activeChar.canOverrideCond(PlayerCondOverride.CHAT_CONDITIONS))
 		{
@@ -62,7 +62,7 @@
 			return;
 		}
 		
-		final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);
+		final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing);
 		for (Player player : World.getInstance().getPlayers())
 		{
 			if ((player != null) && !BlockList.isBlocked(player, activeChar))
Index: java/org/l2jmobius/gameserver/model/holders/SharedTeleportHolder.java
===================================================================
--- java/org/l2jmobius/gameserver/model/holders/SharedTeleportHolder.java	(nonexistent)
+++ java/org/l2jmobius/gameserver/model/holders/SharedTeleportHolder.java	(working copy)
@@ -0,0 +1,75 @@
+/*
+ * This file is part of the L2J Mobius project.
+ * 
+ * This program is free software: you can redistribute it and/or modify
+ * it under the terms of the GNU General Public License as published by
+ * the Free Software Foundation, either version 3 of the License, or
+ * (at your option) any later version.
+ * 
+ * This program is distributed in the hope that it will be useful,
+ * but WITHOUT ANY WARRANTY; without even the implied warranty of
+ * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
+ * General Public License for more details.
+ * 
+ * You should have received a copy of the GNU General Public License
+ * along with this program. If not, see <http://www.gnu.org/licenses/>.
+ */
+package org.l2jmobius.gameserver.model.holders;
+
+/**
+ * @author NasSeKa
+ */
+public class SharedTeleportHolder
+{
+	private final int _tpId;
+	private final String _charName;
+	private int _timesUsed;
+	private final int _x;
+	private final int _y;
+	private final int _z;
+	
+	public SharedTeleportHolder(int tpId, String charName, int timesUsed, int x, int y, int z)
+	{
+		_tpId = tpId;
+		_charName = charName;
+		_timesUsed = timesUsed;
+		_x = x;
+		_y = y;
+		_z = z;
+	}
+	
+	public int getTpId()
+	{
+		return _tpId;
+	}
+	
+	public String getCharName()
+	{
+		return _charName;
+	}
+	
+	public int getTimesUsed()
+	{
+		return _timesUsed;
+	}
+	
+	public void useTeleport()
+	{
+		_timesUsed -= 1;
+	}
+	
+	public int getX()
+	{
+		return _x;
+	}
+	
+	public int getY()
+	{
+		return _y;
+	}
+	
+	public int getZ()
+	{
+		return _z;
+	}
+}
\ No newline at end of file
Index: dist/game/data/scripts/handlers/chathandlers/ChatWhisper.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatWhisper.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatWhisper.java	(working copy)
@@ -41,7 +41,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
 		{
@@ -68,7 +68,7 @@
 				if (Config.FAKE_PLAYER_CHAT)
 				{
 					final String name = FakePlayerData.getInstance().getProperName(target);
-					activeChar.sendPacket(new CreatureSay(activeChar, null, "->" + name, type, text));
+					activeChar.sendPacket(new CreatureSay(activeChar, null, "->" + name, type, text, 0));
 					FakePlayerChatManager.getInstance().manageChat(activeChar, name, text);
 				}
 				else
@@ -120,8 +120,8 @@
 				}
 				
 				receiver.getWhisperers().add(activeChar.getObjectId());
-				receiver.sendPacket(new CreatureSay(activeChar, receiver, activeChar.getName(), type, text));
-				activeChar.sendPacket(new CreatureSay(activeChar, receiver, "->" + receiver.getName(), type, text));
+				receiver.sendPacket(new CreatureSay(activeChar, receiver, activeChar.getName(), type, text, 0));
+				activeChar.sendPacket(new CreatureSay(activeChar, receiver, "->" + receiver.getName(), type, text, 0));
 			}
 			else
 			{
Index: java/org/l2jmobius/gameserver/instancemanager/PetitionManager.java
===================================================================
--- java/org/l2jmobius/gameserver/instancemanager/PetitionManager.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/instancemanager/PetitionManager.java	(working copy)
@@ -332,7 +332,7 @@
 			
 			if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId()))
 			{
-				cs = new CreatureSay(player, ChatType.PETITION_PLAYER, player.getName(), messageText);
+				cs = new CreatureSay(player, ChatType.PETITION_PLAYER, player.getName(), messageText, 0);
 				currPetition.addLogMessage(cs);
 				
 				currPetition.sendResponderPacket(cs);
@@ -342,7 +342,7 @@
 			
 			if ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId()))
 			{
-				cs = new CreatureSay(player, ChatType.PETITION_GM, player.getName(), messageText);
+				cs = new CreatureSay(player, ChatType.PETITION_GM, player.getName(), messageText, 0);
 				currPetition.addLogMessage(cs);
 				
 				currPetition.sendResponderPacket(cs);
@@ -415,7 +415,7 @@
 		
 		// Notify all GMs that a new petition has been submitted.
 		final String msgContent = petitioner.getName() + " has submitted a new petition."; // (ID: " + newPetitionId + ").";
-		AdminData.getInstance().broadcastToGMs(new CreatureSay(petitioner, ChatType.HERO_VOICE, "Petition System", msgContent));
+		AdminData.getInstance().broadcastToGMs(new CreatureSay(petitioner, ChatType.HERO_VOICE, "Petition System", msgContent, 0));
 		return newPetitionId;
 	}
 	
Index: dist/game/data/scripts/handlers/telnethandlers/chat/Msg.java
===================================================================
--- dist/game/data/scripts/handlers/telnethandlers/chat/Msg.java	(revision 10469)
+++ dist/game/data/scripts/handlers/telnethandlers/chat/Msg.java	(working copy)
@@ -56,7 +56,7 @@
 			{
 				sb.append(args[i] + " ");
 			}
-			player.sendPacket(new CreatureSay(null, ChatType.WHISPER, "Telnet Priv", sb.toString()));
+			player.sendPacket(new CreatureSay(null, ChatType.WHISPER, "Telnet Priv", sb.toString(), 0));
 			return "Announcement sent!";
 		}
 		return "Couldn't find player with such name.";
Index: java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharedLocationTeleportUi.java
===================================================================
--- java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharedLocationTeleportUi.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharedLocationTeleportUi.java	(working copy)
@@ -23,13 +23,16 @@
 import org.l2jmobius.gameserver.network.serverpackets.teleports.ExShowSharedLocationTeleportUi;
 
 /**
- * @author GustavoFonseca
+ * @author NasSeKa
  */
 public class ExRequestSharedLocationTeleportUi implements IClientIncomingPacket
 {
+	private int _id;
+	
 	@Override
 	public boolean read(GameClient client, PacketReader packet)
 	{
+		_id = packet.readD();
 		return true;
 	}
 	
@@ -42,6 +45,8 @@
 			return;
 		}
 		
-		client.sendPacket(new ExShowSharedLocationTeleportUi());
+		int tpId = (_id - 1) / 256;
+		
+		client.sendPacket(new ExShowSharedLocationTeleportUi(tpId));
 	}
 }
Index: dist/game/data/scripts/handlers/admincommandhandlers/AdminGmChat.java
===================================================================
--- dist/game/data/scripts/handlers/admincommandhandlers/AdminGmChat.java	(revision 10469)
+++ dist/game/data/scripts/handlers/admincommandhandlers/AdminGmChat.java	(working copy)
@@ -112,7 +112,7 @@
 				offset = 13;
 			}
 			text = command.substring(offset);
-			AdminData.getInstance().broadcastToGMs(new CreatureSay(null, ChatType.ALLIANCE, activeChar.getName(), text));
+			AdminData.getInstance().broadcastToGMs(new CreatureSay(null, ChatType.ALLIANCE, activeChar.getName(), text, 0));
 		}
 		catch (StringIndexOutOfBoundsException e)
 		{
Index: java/org/l2jmobius/gameserver/handler/IChatHandler.java
===================================================================
--- java/org/l2jmobius/gameserver/handler/IChatHandler.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/handler/IChatHandler.java	(working copy)
@@ -31,8 +31,9 @@
 	 * @param player
 	 * @param target
 	 * @param text
+	 * @param isLocSharing
 	 */
-	void handleChat(ChatType type, Player player, String target, String text);
+	void handleChat(ChatType type, Player player, String target, String text, int isLocSharing);
 	
 	/**
 	 * Returns a list of all chat types registered to this handler
Index: java/org/l2jmobius/gameserver/instancemanager/SharedTeleportManager.java
===================================================================
--- java/org/l2jmobius/gameserver/instancemanager/SharedTeleportManager.java	(nonexistent)
+++ java/org/l2jmobius/gameserver/instancemanager/SharedTeleportManager.java	(working copy)
@@ -0,0 +1,141 @@
+/*
+ * This file is part of the L2J Mobius project.
+ * 
+ * This program is free software: you can redistribute it and/or modify
+ * it under the terms of the GNU General Public License as published by
+ * the Free Software Foundation, either version 3 of the License, or
+ * (at your option) any later version.
+ * 
+ * This program is distributed in the hope that it will be useful,
+ * but WITHOUT ANY WARRANTY; without even the implied warranty of
+ * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
+ * General Public License for more details.
+ * 
+ * You should have received a copy of the GNU General Public License
+ * along with this program. If not, see <http://www.gnu.org/licenses/>.
+ */
+package org.l2jmobius.gameserver.instancemanager;
+
+import java.util.Map;
+import java.util.concurrent.ConcurrentHashMap;
+import java.util.logging.Logger;
+
+import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.holders.SharedTeleportHolder;
+
+/**
+ * Shared Teleport Manager
+ * @author NasSeKa
+ */
+public class SharedTeleportManager
+{
+	protected static final Logger LOGGER = Logger.getLogger(PetitionManager.class.getName());
+	private int _lastSharedTeleportId = 0;
+	private final Map<Integer, SharedTeleportHolder> _sharedTeleports = new ConcurrentHashMap<>();
+	
+	protected SharedTeleportManager()
+	{
+		init();
+	}
+	
+	public void init()
+	{
+		_sharedTeleports.clear();
+		LOGGER.info(getClass().getSimpleName() + ": Shared Teleport data cleared.");
+	}
+	
+	public Map<Integer, SharedTeleportHolder> getSharedTeleports()
+	{
+		return _sharedTeleports;
+	}
+	
+	public void addSharedTeleport(Player player)
+	{
+		int freeId = getNextFreeID();
+		_sharedTeleports.put(freeId, new SharedTeleportHolder(freeId, player.getName(), 5, player.getX(), player.getY(), player.getZ()));
+	}
+	
+	public int getNextFreeID()
+	{
+		_lastSharedTeleportId += 1;
+		return _lastSharedTeleportId;
+	}
+	
+	public int getLastSharedId()
+	{
+		return _lastSharedTeleportId;
+	}
+	
+	public void teleportToSharedLocation(Player player, int id)
+	{
+		if ((id == 0) || (getTimesUsed(id) == 0) || (player == null))
+		{
+			return;
+		}
+		
+		_sharedTeleports.get(id).useTeleport();
+		player.abortCast();
+		player.stopMove(null);
+		player.teleToLocation(getX(id), getY(id), getZ(id));
+	}
+	
+	public String getCharName(int id)
+	{
+		if (id == 0)
+		{
+			return "";
+		}
+		
+		return _sharedTeleports.get(id).getCharName();
+	}
+	
+	public int getTimesUsed(int id)
+	{
+		if (_sharedTeleports.getOrDefault(id, null) == null)
+		{
+			return 0;
+		}
+		return _sharedTeleports.get(id).getTimesUsed();
+	}
+	
+	public int getX(int id)
+	{
+		if (_sharedTeleports.getOrDefault(id, null) == null)
+		{
+			return 0;
+		}
+		return _sharedTeleports.get(id).getX();
+	}
+	
+	public int getY(int id)
+	{
+		if (_sharedTeleports.getOrDefault(id, null) == null)
+		{
+			return 0;
+		}
+		return _sharedTeleports.get(id).getY();
+	}
+	
+	public int getZ(int id)
+	{
+		if (_sharedTeleports.getOrDefault(id, null) == null)
+		{
+			return 0;
+		}
+		return _sharedTeleports.get(id).getZ();
+	}
+	
+	/**
+	 * Gets the single instance of {@code SharedTeleportManager}.
+	 * @return single instance of {@code SharedTeleportManager}
+	 */
+	public static SharedTeleportManager getInstance()
+	{
+		return SingletonHolder.INSTANCE;
+	}
+	
+	private static class SingletonHolder
+	{
+		protected static final SharedTeleportManager INSTANCE = new SharedTeleportManager();
+	}
+}
Index: java/org/l2jmobius/gameserver/network/clientpackets/EnterWorld.java
===================================================================
--- java/org/l2jmobius/gameserver/network/clientpackets/EnterWorld.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/network/clientpackets/EnterWorld.java	(working copy)
@@ -492,7 +492,7 @@
 		
 		if ((Config.SERVER_RESTART_SCHEDULE_ENABLED) && (Config.SERVER_RESTART_SCHEDULE_MESSAGE))
 		{
-			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "[SERVER]", "Next restart is scheduled at " + ServerRestartManager.getInstance().getNextRestartTime() + "."));
+			player.sendPacket(new CreatureSay(null, ChatType.BATTLEFIELD, "[SERVER]", "Next restart is scheduled at " + ServerRestartManager.getInstance().getNextRestartTime() + ".", 0));
 		}
 		
 		if (showClanNotice)
Index: java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharingLocationUi.java
===================================================================
--- java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharingLocationUi.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/network/clientpackets/teleports/ExRequestSharingLocationUi.java	(working copy)
@@ -42,6 +42,6 @@
 			return;
 		}
 		
-		client.sendPacket(new ExShowSharingLocationUi());
+		player.sendPacket(new ExShowSharingLocationUi());
 	}
 }
Index: dist/game/data/scripts/handlers/chathandlers/ChatParty.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatParty.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatParty.java	(working copy)
@@ -21,6 +21,8 @@
 import org.l2jmobius.gameserver.enums.PlayerCondOverride;
 import org.l2jmobius.gameserver.handler.IChatHandler;
 import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
+import org.l2jmobius.gameserver.model.zone.ZoneId;
 import org.l2jmobius.gameserver.network.SystemMessageId;
 import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
 
@@ -36,7 +38,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (!activeChar.isInParty())
 		{
@@ -54,7 +56,25 @@
 			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
 			return;
 		}
-		activeChar.getParty().broadcastCreatureSay(new CreatureSay(activeChar, type, activeChar.getName(), text), activeChar);
+		
+		if ((isLocSharing == 1) && (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.SHARING_LOCATION_COST))
+		{
+			activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
+			return;
+		}
+		
+		if ((isLocSharing == 1) && (activeChar.isInInstance() || activeChar.isInsideZone(ZoneId.SIEGE)))
+		{
+			activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
+			return;
+		}
+		
+		if (isLocSharing == 1)
+		{
+			activeChar.destroyItemByItemId("TeleToSharedLoc", Inventory.LCOIN_ID, Config.SHARING_LOCATION_COST, activeChar, true);
+		}
+		
+		activeChar.getParty().broadcastCreatureSay(new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing), activeChar);
 	}
 	
 	@Override
Index: dist/game/data/scripts/custom/events/Race/Race.java
===================================================================
--- dist/game/data/scripts/custom/events/Race/Race.java	(revision 10469)
+++ dist/game/data/scripts/custom/events/Race/Race.java	(working copy)
@@ -376,7 +376,7 @@
 	
 	private void sendMessage(Player player, String text)
 	{
-		player.sendPacket(new CreatureSay(_npc, ChatType.MPCC_ROOM, _npc.getName(), text));
+		player.sendPacket(new CreatureSay(_npc, ChatType.MPCC_ROOM, _npc.getName(), text, 0));
 	}
 	
 	private void showMenu(Player player)
Index: java/org/l2jmobius/gameserver/util/Broadcast.java
===================================================================
--- java/org/l2jmobius/gameserver/util/Broadcast.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/util/Broadcast.java	(working copy)
@@ -200,7 +200,7 @@
 	
 	public static void toAllOnlinePlayers(String text, boolean isCritical)
 	{
-		toAllOnlinePlayers(new CreatureSay(null, isCritical ? ChatType.CRITICAL_ANNOUNCE : ChatType.ANNOUNCEMENT, "", text));
+		toAllOnlinePlayers(new CreatureSay(null, isCritical ? ChatType.CRITICAL_ANNOUNCE : ChatType.ANNOUNCEMENT, "", text, 0));
 	}
 	
 	public static void toAllOnlinePlayersOnScreen(String text)
Index: java/org/l2jmobius/gameserver/network/clientpackets/RequestPetitionCancel.java
===================================================================
--- java/org/l2jmobius/gameserver/network/clientpackets/RequestPetitionCancel.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/network/clientpackets/RequestPetitionCancel.java	(working copy)
@@ -77,7 +77,7 @@
 				
 				// Notify all GMs that the player's pending petition has been cancelled.
 				final String msgContent = player.getName() + " has canceled a pending petition.";
-				AdminData.getInstance().broadcastToGMs(new CreatureSay(player, ChatType.HERO_VOICE, "Petition System", msgContent));
+				AdminData.getInstance().broadcastToGMs(new CreatureSay(player, ChatType.HERO_VOICE, "Petition System", msgContent, 0));
 			}
 			else
 			{
Index: dist/game/data/scripts/handlers/admincommandhandlers/AdminAdmin.java
===================================================================
--- dist/game/data/scripts/handlers/admincommandhandlers/AdminAdmin.java	(revision 10469)
+++ dist/game/data/scripts/handlers/admincommandhandlers/AdminAdmin.java	(working copy)
@@ -296,7 +296,7 @@
 						sb.append(" ");
 					}
 					
-					final CreatureSay cs = new CreatureSay(activeChar, ChatType.WORLD, activeChar.getName(), sb.toString());
+					final CreatureSay cs = new CreatureSay(activeChar, ChatType.WORLD, activeChar.getName(), sb.toString(), 0);
 					for (Player player : World.getInstance().getPlayers())
 					{
 						if (player.isNotBlocked(activeChar))
Index: dist/game/data/scripts/handlers/bypasshandlers/FindPvP.java
===================================================================
--- dist/game/data/scripts/handlers/bypasshandlers/FindPvP.java	(revision 10469)
+++ dist/game/data/scripts/handlers/bypasshandlers/FindPvP.java	(working copy)
@@ -124,7 +124,7 @@
 				
 				if (biggestAllyId == allyId)
 				{
-					player.sendPacket(new CreatureSay(null, ChatType.WHISPER, target.getName(), "Sorry, your clan/ally is outnumbering the place already so you can't move there."));
+					player.sendPacket(new CreatureSay(null, ChatType.WHISPER, target.getName(), "Sorry, your clan/ally is outnumbering the place already so you can't move there.", 0));
 					return true;
 				}
 			}
@@ -139,7 +139,7 @@
 		}
 		else
 		{
-			player.sendPacket(new CreatureSay(null, ChatType.WHISPER, target.getName(), "Sorry, I can't find anyone in flag status right now."));
+			player.sendPacket(new CreatureSay(null, ChatType.WHISPER, target.getName(), "Sorry, I can't find anyone in flag status right now.", 0));
 		}
 		return false;
 	}
Index: dist/game/data/scripts/handlers/chathandlers/ChatGeneral.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatGeneral.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatGeneral.java	(working copy)
@@ -27,6 +27,8 @@
 import org.l2jmobius.gameserver.model.BlockList;
 import org.l2jmobius.gameserver.model.World;
 import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
+import org.l2jmobius.gameserver.model.zone.ZoneId;
 import org.l2jmobius.gameserver.network.SystemMessageId;
 import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
 import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
@@ -43,7 +45,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String paramsValue, String text)
+	public void handleChat(ChatType type, Player activeChar, String paramsValue, String text, int isLocSharing)
 	{
 		boolean vcdUsed = false;
 		if (text.startsWith("."))
@@ -87,8 +89,26 @@
 				return;
 			}
 			
-			final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getAppearance().getVisibleName(), text);
-			final CreatureSay csRandom = new CreatureSay(activeChar, type, activeChar.getAppearance().getVisibleName(), ChatRandomizer.randomize(text));
+			if ((isLocSharing == 1) && (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.SHARING_LOCATION_COST))
+			{
+				activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
+				return;
+			}
+			
+			if ((isLocSharing == 1) && (activeChar.isInInstance() || activeChar.isInsideZone(ZoneId.SIEGE)))
+			{
+				activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
+				return;
+			}
+			
+			if (isLocSharing == 1)
+			{
+				activeChar.destroyItemByItemId("TeleToSharedLoc", Inventory.LCOIN_ID, Config.SHARING_LOCATION_COST, activeChar, true);
+			}
+			
+			final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getAppearance().getVisibleName(), text, isLocSharing);
+			final CreatureSay csRandom = new CreatureSay(activeChar, type, activeChar.getAppearance().getVisibleName(), ChatRandomizer.randomize(text), isLocSharing);
+			
 			World.getInstance().forEachVisibleObjectInRange(activeChar, Player.class, 1250, player ->
 			{
 				if ((player != null) && !BlockList.isBlocked(player, activeChar))
Index: dist/game/data/scripts/handlers/chathandlers/ChatTrade.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatTrade.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatTrade.java	(working copy)
@@ -24,6 +24,8 @@
 import org.l2jmobius.gameserver.model.BlockList;
 import org.l2jmobius.gameserver.model.World;
 import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
+import org.l2jmobius.gameserver.model.zone.ZoneId;
 import org.l2jmobius.gameserver.network.SystemMessageId;
 import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
 import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
@@ -40,7 +42,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
 		{
@@ -58,7 +60,24 @@
 			return;
 		}
 		
-		final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);
+		if ((isLocSharing == 1) && (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.SHARING_LOCATION_COST))
+		{
+			activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
+			return;
+		}
+		
+		if ((isLocSharing == 1) && (activeChar.isInInstance() || activeChar.isInsideZone(ZoneId.SIEGE)))
+		{
+			activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
+			return;
+		}
+		
+		if (isLocSharing == 1)
+		{
+			activeChar.destroyItemByItemId("TeleToSharedLoc", Inventory.LCOIN_ID, Config.SHARING_LOCATION_COST, activeChar, true);
+		}
+		
+		final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing);
 		if (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.canOverrideCond(PlayerCondOverride.CHAT_CONDITIONS)))
 		{
 			final int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
Index: dist/game/data/scripts/handlers/chathandlers/ChatClan.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatClan.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatClan.java	(working copy)
@@ -21,6 +21,8 @@
 import org.l2jmobius.gameserver.enums.PlayerCondOverride;
 import org.l2jmobius.gameserver.handler.IChatHandler;
 import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
+import org.l2jmobius.gameserver.model.zone.ZoneId;
 import org.l2jmobius.gameserver.network.SystemMessageId;
 import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
 
@@ -36,7 +38,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (activeChar.getClan() == null)
 		{
@@ -54,7 +56,25 @@
 			activeChar.sendPacket(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED);
 			return;
 		}
-		activeChar.getClan().broadcastCSToOnlineMembers(new CreatureSay(activeChar, type, activeChar.getName(), text), activeChar);
+		
+		if ((isLocSharing == 1) && (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.SHARING_LOCATION_COST))
+		{
+			activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
+			return;
+		}
+		
+		if ((isLocSharing == 1) && (activeChar.isInInstance() || activeChar.isInsideZone(ZoneId.SIEGE)))
+		{
+			activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
+			return;
+		}
+		
+		if (isLocSharing == 1)
+		{
+			activeChar.destroyItemByItemId("TeleToSharedLoc", Inventory.LCOIN_ID, Config.SHARING_LOCATION_COST, activeChar, true);
+		}
+		
+		activeChar.getClan().broadcastCSToOnlineMembers(new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing), activeChar);
 	}
 	
 	@Override
Index: dist/game/data/scripts/handlers/chathandlers/ChatShout.java
===================================================================
--- dist/game/data/scripts/handlers/chathandlers/ChatShout.java	(revision 10469)
+++ dist/game/data/scripts/handlers/chathandlers/ChatShout.java	(working copy)
@@ -24,6 +24,8 @@
 import org.l2jmobius.gameserver.model.BlockList;
 import org.l2jmobius.gameserver.model.World;
 import org.l2jmobius.gameserver.model.actor.Player;
+import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
+import org.l2jmobius.gameserver.model.zone.ZoneId;
 import org.l2jmobius.gameserver.network.SystemMessageId;
 import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
 import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
@@ -40,7 +42,7 @@
 	};
 	
 	@Override
-	public void handleChat(ChatType type, Player activeChar, String target, String text)
+	public void handleChat(ChatType type, Player activeChar, String target, String text, int isLocSharing)
 	{
 		if (activeChar.isChatBanned() && Config.BAN_CHAT_CHANNELS.contains(type))
 		{
@@ -58,7 +60,24 @@
 			return;
 		}
 		
-		final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text);
+		if ((isLocSharing == 1) && (activeChar.getInventory().getInventoryItemCount(Inventory.LCOIN_ID, -1) < Config.SHARING_LOCATION_COST))
+		{
+			activeChar.sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_L_COINS);
+			return;
+		}
+		
+		if ((isLocSharing == 1) && (activeChar.isInInstance() || activeChar.isInsideZone(ZoneId.SIEGE)))
+		{
+			activeChar.sendPacket(SystemMessageId.LOCATION_CANNOT_BE_SHARED_SINCE_THE_CONDITIONS_ARE_NOT_MET);
+			return;
+		}
+		
+		if (isLocSharing == 1)
+		{
+			activeChar.destroyItemByItemId("TeleToSharedLoc", Inventory.LCOIN_ID, Config.SHARING_LOCATION_COST, activeChar, true);
+		}
+		
+		final CreatureSay cs = new CreatureSay(activeChar, type, activeChar.getName(), text, isLocSharing);
 		if (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("on") || (Config.DEFAULT_GLOBAL_CHAT.equalsIgnoreCase("gm") && activeChar.canOverrideCond(PlayerCondOverride.CHAT_CONDITIONS)))
 		{
 			final int region = MapRegionManager.getInstance().getMapRegionLocId(activeChar);
Index: java/org/l2jmobius/gameserver/network/clientpackets/Say2.java
===================================================================
--- java/org/l2jmobius/gameserver/network/clientpackets/Say2.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/network/clientpackets/Say2.java	(working copy)
@@ -87,6 +87,7 @@
 	private String _text;
 	private int _type;
 	private String _target;
+	private int _isLocSharing;
 	
 	@Override
 	public boolean read(GameClient client, PacketReader packet)
@@ -93,10 +94,11 @@
 	{
 		_text = packet.readS();
 		_type = packet.readD();
+		_isLocSharing = packet.readC();
 		if (_type == ChatType.WHISPER.getClientId())
 		{
-			packet.readC();
 			_target = packet.readS();
+			_isLocSharing = 0;
 		}
 		return true;
 	}
@@ -211,7 +213,7 @@
 		final IChatHandler handler = ChatHandler.getInstance().getHandler(chatType);
 		if (handler != null)
 		{
-			handler.handleChat(chatType, player, _target, _text);
+			handler.handleChat(chatType, player, _target, _text, _isLocSharing);
 		}
 		else
 		{
Index: java/org/l2jmobius/gameserver/instancemanager/FakePlayerChatManager.java
===================================================================
--- java/org/l2jmobius/gameserver/instancemanager/FakePlayerChatManager.java	(revision 10469)
+++ java/org/l2jmobius/gameserver/instancemanager/FakePlayerChatManager.java	(working copy)
@@ -179,7 +179,7 @@
 			final Npc npc = spawn.getLastSpawn();
 			if (npc != null)
 			{
-				player.sendPacket(new CreatureSay(npc, ChatType.WHISPER, fpcName, message));
+				player.sendPacket(new CreatureSay(npc, ChatType.WHISPER, fpcName, message, 0));
 			}
 		}
 	}
