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
package handlers.effecthandlers;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.SubclassInfoType;
import org.l2jmobius.gameserver.model.Shortcut;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.AcquireSkillList;
import org.l2jmobius.gameserver.network.serverpackets.ExStorageMaxCount;
import org.l2jmobius.gameserver.network.serverpackets.ExSubjobInfo;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowAll;
import org.l2jmobius.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.taskmanager.AutoUseTaskManager;

/**
 * @author Sdw
 */
public class ClassChange extends AbstractEffect
{
	private static final int IDENTITY_CRISIS_SKILL_ID = 1570;
	
	private final int _index;
	
	public ClassChange(StatSet params)
	{
		_index = params.getInt("index", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effected.isPlayer())
		{
			return;
		}
		
		// Executing later otherwise interrupted exception during storeCharBase.
		ThreadPool.schedule(() ->
		{
			final Player player = effected.getActingPlayer();
			if (player.isTransformed() || player.isSubclassLocked() || player.isAffectedBySkill(IDENTITY_CRISIS_SKILL_ID))
			{
				player.sendMessage("You cannot switch your class right now!");
				return;
			}
			
			final Skill identityCrisis = SkillData.getInstance().getSkill(IDENTITY_CRISIS_SKILL_ID, 1);
			if (identityCrisis != null)
			{
				identityCrisis.applyEffects(player, player);
			}
			
			if (OlympiadManager.getInstance().isRegisteredInComp(player))
			{
				OlympiadManager.getInstance().unRegisterNoble(player);
			}
			
			final int activeClass = player.getClassId().getId();
			player.setActiveClass(_index);
			
			final SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCESSFULLY_SWITCHED_S1_TO_S2);
			msg.addClassId(activeClass);
			msg.addClassId(player.getClassId().getId());
			player.sendPacket(msg);
			
			player.broadcastUserInfo();
			player.sendPacket(new ExStorageMaxCount(player));
			player.sendPacket(new AcquireSkillList(player));
			player.sendPacket(new ExSubjobInfo(player, SubclassInfoType.CLASS_CHANGED));
			
			if (player.isInParty())
			{
				// Delete party window for other party members
				player.getParty().broadcastToPartyMembers(player, PartySmallWindowDeleteAll.STATIC_PACKET);
				for (Player member : player.getParty().getMembers())
				{
					// And re-add
					if (member != player)
					{
						member.sendPacket(new PartySmallWindowAll(member, player.getParty()));
					}
				}
			}
			
			// Stop auto use.
			for (Shortcut shortcut : player.getAllShortCuts())
			{
				if (!shortcut.isAutoUse())
				{
					continue;
				}
				
				player.removeAutoShortcut(shortcut.getSlot(), shortcut.getPage());
				
				if (player.getAutoUseSettings().isAutoSkill(shortcut.getId()))
				{
					final Skill knownSkill = player.getKnownSkill(shortcut.getId());
					if (knownSkill != null)
					{
						if (knownSkill.isBad())
						{
							AutoUseTaskManager.getInstance().removeAutoSkill(player, shortcut.getId());
						}
						else
						{
							AutoUseTaskManager.getInstance().removeAutoBuff(player, shortcut.getId());
						}
					}
				}
				else
				{
					final Item knownItem = player.getInventory().getItemByObjectId(shortcut.getId());
					if (knownItem != null)
					{
						if (knownItem.isPotion())
						{
							AutoUseTaskManager.getInstance().removeAutoPotionItem(player, knownItem.getId());
						}
						else
						{
							AutoUseTaskManager.getInstance().removeAutoSupplyItem(player, knownItem.getId());
						}
					}
				}
			}
			
			// Fix Death Knight model animation.
			if (player.isDeathKnight())
			{
				player.transform(101, false);
				ThreadPool.schedule(() -> player.stopTransformation(false), 50);
			}
		}, 500);
	}
}
