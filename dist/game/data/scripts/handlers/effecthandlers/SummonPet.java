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

import java.util.logging.Level;

import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.data.xml.PetDataTable;
import org.l2jmobius.gameserver.enums.EvolveLevel;
import org.l2jmobius.gameserver.model.PetData;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.holders.PetEvolveHolder;
import org.l2jmobius.gameserver.model.holders.PetItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Summon Pet effect implementation.
 * @author UnAfraid
 */
public class SummonPet extends AbstractEffect
{
	public SummonPet(StatSet params)
	{
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SUMMON_PET;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effector.isPlayer() || !effected.isPlayer() || effected.isAlikeDead())
		{
			return;
		}
		
		final Player player = effector.getActingPlayer();
		if (player.hasPet() || player.isMounted())
		{
			player.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return;
		}
		
		final PetItemHolder holder = player.removeScript(PetItemHolder.class);
		if (holder == null)
		{
			LOGGER.log(Level.WARNING, "Summoning pet without attaching PetItemHandler!", new Throwable());
			return;
		}
		
		final Item collar = holder.getItem();
		if (player.getInventory().getItemByObjectId(collar.getObjectId()) != collar)
		{
			LOGGER.warning("Player: " + player + " is trying to summon pet from item that he doesn't owns.");
			return;
		}
		
		final PetEvolveHolder evolveData = player.getPetEvolve(collar.getObjectId());
		final PetData petData = evolveData.getEvolve() == EvolveLevel.None ? PetDataTable.getInstance().getPetDataByEvolve(collar.getId(), evolveData.getEvolve()) : PetDataTable.getInstance().getPetDataByEvolve(collar.getId(), evolveData.getEvolve(), evolveData.getIndex());
		if ((petData == null) || (petData.getNpcId() == -1))
		{
			return;
		}
		
		final NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(petData.getNpcId());
		final Pet pet = Pet.spawnPet(npcTemplate, player, collar);
		player.setPet(pet);
		pet.setShowSummonAnimation(true);
		if (!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp());
			pet.setCurrentMp(pet.getMaxMp());
			pet.getStat().setExp(pet.getExpForThisLevel());
			pet.setCurrentFed(pet.getMaxFed());
			pet.storeMe();
		}
		pet.setRunning();
		collar.setEnchantLevel(pet.getLevel());
		pet.spawnMe(player.getX() + 50, player.getY() + 100, player.getZ());
		pet.startFeed();
	}
}
