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
package ai.others.AttributeMaster;

import java.util.Arrays;

import org.l2jmobius.gameserver.enums.UserInfoType;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnElementalSpiritLearn;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.elementalspirits.ElementalSpiritInfo;

import ai.AbstractNpcAI;

/**
 * @author JoeAlisson
 */
public class AttributeMaster extends AbstractNpcAI
{
	private static final int SVEIN = 34053;
	
	private AttributeMaster()
	{
		addStartNpc(SVEIN);
		addTalkId(SVEIN);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if ("learn".equalsIgnoreCase(event))
		{
			if ((player.getLevel() < 76) || (player.getClassId().level() < 3))
			{
				return "no-3rdClass.htm";
			}
			
			if (player.getSpirits() == null)
			{
				player.initElementalSpirits();
			}
			
			if (Arrays.stream(player.getSpirits()).allMatch(elementalSpirit -> elementalSpirit.getStage() > 0))
			{
				return "already.htm";
			}
			
			for (ElementalSpirit spirit : player.getSpirits())
			{
				if (spirit.getStage() == 0)
				{
					spirit.upgrade();
				}
			}
			final UserInfo userInfo = new UserInfo(player);
			userInfo.addComponentType(UserInfoType.ATT_SPIRITS);
			player.sendPacket(userInfo);
			player.sendPacket(new ElementalSpiritInfo(player, player.getActiveElementalSpiritType(), (byte) 0x01));
			EventDispatcher.getInstance().notifyEventAsync(new OnElementalSpiritLearn(player), player);
			return "learn.htm";
		}
		return null;
	}
	
	public static AbstractNpcAI provider()
	{
		return new AttributeMaster();
	}
}
