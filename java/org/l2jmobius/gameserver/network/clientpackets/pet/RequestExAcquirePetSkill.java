package org.l2jmobius.gameserver.network.clientpackets.pet;

import java.util.Optional;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.PetAcquireList;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.holders.PetSkillAcquireHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.pet.ExPetSkillList;

/**
 * Written by Berezkin Nikolay, on 26.04.2021
 */
public class RequestExAcquirePetSkill implements IClientIncomingPacket
{
	private int skillId, skillLevel;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		skillId = packet.readD();
		skillLevel = packet.readD();
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
		final Pet pet = player.getPet();
		if (pet == null)
		{
			return;
		}
		final Skill skill = SkillData.getInstance().getSkill(skillId, skillLevel);
		if (skill == null)
		{
			return;
		}
		final Optional<PetSkillAcquireHolder> reqItem = PetAcquireList.getInstance().getSkills(pet.getPetData().getType()).stream().filter(it -> (it.getSkillId() == skillId) && (it.getSkillLevel() == skillLevel)).findFirst();
		if (reqItem.isPresent())
		{
			if (reqItem.get().getItem() != null)
			{
				if (player.destroyItemByItemId("PetAcquireSkill", reqItem.get().getItem().getId(), reqItem.get().getItem().getCount(), null, true))
				{
					pet.addSkill(skill);
					pet.storePetSkills(skillId, skillLevel);
					player.sendPacket(new ExPetSkillList(false, pet));
				}
			}
			else
			{
				pet.addSkill(skill);
				player.sendPacket(new ExPetSkillList(false, pet));
			}
		}
	}
}
