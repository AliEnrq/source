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
package org.l2jmobius.gameserver.network.clientpackets.classchange;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.classchange.ExClassChangeSetAlarm;

/**
 * @author Mobius
 */
public class ExRequestClassChangeVerifying implements IClientIncomingPacket
{
	private int _classId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_classId = packet.readD();
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
		
		if (_classId != player.getClassId().getId())
		{
			return;
		}
		
		if (player.isInCategory(CategoryType.FOURTH_CLASS_GROUP))
		{
			return;
		}
		
		if (player.isInCategory(CategoryType.THIRD_CLASS_GROUP))
		{
			if (!thirdClassCheck(player))
			{
				return;
			}
		}
		else if (player.isInCategory(CategoryType.SECOND_CLASS_GROUP))
		{
			if (!secondClassCheck(player))
			{
				return;
			}
		}
		else if (player.isInCategory(CategoryType.FIRST_CLASS_GROUP))
		{
			if (!firstClassCheck(player))
			{
				return;
			}
		}
		
		player.sendPacket(ExClassChangeSetAlarm.STATIC_PACKET);
	}
	
	private boolean firstClassCheck(Player player)
	{
		QuestState qs = null;
		if (player.isDeathKnight())
		{
			qs = player.getQuestState("Q10959_ChallengingYourDestiny");
		}
		else
		{
			switch (player.getRace())
			{
				case HUMAN:
				{
					qs = player.getQuestState("Q10982_SpiderHunt");
					break;
				}
				case ELF:
				{
					qs = player.getQuestState("Q10984_CollectSpiderweb");
					break;
				}
				case DARK_ELF:
				{
					qs = player.getQuestState("Q10986_SwampMonster");
					break;
				}
				case ORC:
				{
					qs = player.getQuestState("Q10988_Conspiracy");
					break;
				}
				case DWARF:
				{
					qs = player.getQuestState("Q10990_PoisonExtraction");
					break;
				}
				case KAMAEL:
				{
					qs = player.getQuestState("Q10962_NewHorizons");
					break;
				}
				case SYLPH:
				{
					qs = player.getQuestState("Q10956_WeSylphs");
					break;
				}
			}
		}
		return (qs != null) && qs.isCompleted();
	}
	
	private boolean secondClassCheck(Player player)
	{
		// SecondClassChange.java has only level check.
		return player.getLevel() >= 40;
	}
	
	private boolean thirdClassCheck(Player player)
	{
		final QuestState qs = player.getQuestState("Q10673_SagaOfLegend");
		return (qs != null) && qs.isCompleted();
	}
}