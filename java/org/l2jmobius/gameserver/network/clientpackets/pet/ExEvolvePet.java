package org.l2jmobius.gameserver.network.clientpackets.pet;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.NpcData;
import org.l2jmobius.gameserver.data.xml.PetDataTable;
import org.l2jmobius.gameserver.data.xml.PetTypesListData;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.EvolveLevel;
import org.l2jmobius.gameserver.model.PetData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;

/**
 * Written by Berezkin Nikolay, on 25.04.2021
 */
public class ExEvolvePet implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player activeChar = client.getPlayer();
		if (activeChar == null)
		{
			return;
		}
		
		final Pet pet = activeChar.getPet();
		if (pet == null)
		{
			return;
		}
		
		if (!activeChar.isMounted() && !pet.isDead() && !activeChar.isDead() && !pet.isHungry() && !activeChar.isControlBlocked() && !activeChar.isInDuel() && !activeChar.isSitting() && !activeChar.isFishing() && !activeChar.isInCombat() && !pet.isInCombat())
		{
			final boolean isAbleToEvolveLevel1 = (pet.getLevel() >= 40) && (pet.getEvolveLevel() == EvolveLevel.None.ordinal());
			final boolean isAbleToEvolveLevel2 = (pet.getLevel() >= 76) && (pet.getEvolveLevel() == EvolveLevel.First.ordinal());
			
			if (isAbleToEvolveLevel1 && activeChar.destroyItemByItemId("PetEvolve", 94096, 1, null, true))
			{
				doEvolve(activeChar, pet, EvolveLevel.First);
			}
			else if (isAbleToEvolveLevel2 && activeChar.destroyItemByItemId("PetEvolve", 94117, 1, null, true))
			{
				doEvolve(activeChar, pet, EvolveLevel.Second);
			}
		}
		else
		{
			activeChar.sendMessage("You can't evolve in this time."); // TODO: Proper system messages.
		}
	}
	
	private void doEvolve(Player activeChar, Pet pet, EvolveLevel evolveLevel)
	{
		final Item controlItem = pet.getControlItem();
		pet.unSummon(activeChar);
		final List<PetData> pets = PetDataTable.getInstance().getPetDatasByEvolve(controlItem.getId(), evolveLevel);
		final PetData targetPet = pets.get(Rnd.get(pets.size()));
		final PetData petData = PetDataTable.getInstance().getPetData(targetPet.getNpcId());
		if ((petData == null) || (petData.getNpcId() == -1))
		{
			return;
		}
		
		final NpcTemplate npcTemplate = NpcData.getInstance().getTemplate(evolveLevel == EvolveLevel.Second ? pet.getId() + 2 : petData.getNpcId());
		final Pet evolved = Pet.spawnPet(npcTemplate, activeChar, controlItem);
		
		if (evolved == null)
		{
			return;
		}
		activeChar.setPet(evolved);
		evolved.setShowSummonAnimation(true);
		evolved.setEvolveLevel(evolveLevel);
		evolved.setRunning();
		evolved.storeEvolvedPets(evolveLevel.ordinal(), evolved.getPetData().getIndex(), controlItem.getObjectId());
		controlItem.setEnchantLevel(evolved.getLevel());
		if (evolveLevel == EvolveLevel.First)
		{
			final List<Entry<Integer, Entry<Integer, Integer>>> specialTypes = PetTypesListData.getInstance().getTypes().entrySet().stream().filter(it -> it.getValue().getKey() != 0).collect(Collectors.toList());
			final int randomIndex = Rnd.get(specialTypes.size() - 1);
			evolved.addSkill(SkillData.getInstance().getSkill(specialTypes.get(randomIndex).getValue().getKey(), specialTypes.get(randomIndex).getValue().getValue()));
		}
		evolved.spawnMe(pet.getX(), pet.getY(), pet.getZ());
		evolved.startFeed();
	}
}
