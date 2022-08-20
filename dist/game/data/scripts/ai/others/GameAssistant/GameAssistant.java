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
package ai.others.GameAssistant;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerBypass;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.itemcontainer.PlayerFreight;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExPremiumManagerShowHtml;
import org.l2jmobius.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import org.l2jmobius.gameserver.network.serverpackets.PackageToList;
import org.l2jmobius.gameserver.network.serverpackets.WareHouseWithdrawalList;
import org.l2jmobius.gameserver.network.serverpackets.ensoul.ExShowEnsoulExtractionWindow;
import org.l2jmobius.gameserver.network.serverpackets.ensoul.ExShowEnsoulWindow;
import org.l2jmobius.gameserver.network.serverpackets.equipmentupgrade.ExShowUpgradeSystem;

import ai.AbstractNpcAI;

/**
 * Dimensional Merchant AI.
 * @author Mobius, quangnguyen, manax
 */
public class GameAssistant extends AbstractNpcAI
{
	// NPC
	private static final int MERCHANT = 32478;
	// Items
	private static final int BLACK_SAYHA_CLOAK = 91210;
	private static final int WHITE_SAYHA_CLOAK = 91211;
	private static final int RED_SAYHA_CLOAK = 91212;
	private static final int PACKAGE_CLOAK = 93303;
	private static final int SAYHA_CLOAK_COUPON = 91227;
	private static final int ADVENTURER_MARK_LV_1 = 91654;
	private static final int ADVENTURER_MARK_LV_2 = 91655;
	private static final int ADVENTURER_MARK_LV_3 = 91656;
	private static final int ADVENTURER_MARK_LV_4 = 91657;
	private static final int ADVENTURER_MARK_LV_5 = 91931;
	// Multisells
	private static final int ATTENDANCE_REWARD_MULTISELL = 3247801;
	private static final int EX_BOSS_WEAPON_SHOP = 3247813;
	private static final int EX_LA_VIE_EN_ROSE = 3247841;
	private static final int EX_HEAVY_A_GRADE = 3247821;
	private static final int EX_LIGHT_A_GRADE = 3247822;
	private static final int EX_ROBE_A_GRADE = 3247823;
	private static final int EX_WEAPON_A_GRADE = 3247824;
	private static final int EX_SPECIAL_A_GRADE = 3247825;
	private static final int EX_HEAVY_B_GRADE = 3247826;
	private static final int EX_LIGHT_B_GRADE = 3247827;
	private static final int EX_ROBE_B_GRADE = 3247828;
	private static final int EX_WEAPON_B_GRADE = 3247829;
	private static final int EX_WEAPON_7_B_GRADE = 3247840;
	private static final int EX_WEAPON_C_GRADE = 3247842;
	private static final int EX_ARMOR_C_GRADE = 3247830;
	private static final int EX_ARMOR_4_C_GRADE = 3247831;
	private static final int EX_AGATHION_SPIRIT = 3247835;
	private static final int EX_SOULSHOT = 3247838;
	private static final int EX_DYES1 = 3247843;
	private static final int EX_DYES2 = 3247844;
	private static final int EX_DYES3 = 3247845;
	private static final int EX_DYES4 = 3247846;
	// Others
	private static final String COMMAND_BYPASS = "Quest GameAssistant ";
	
	private GameAssistant()
	{
		addStartNpc(MERCHANT);
		addFirstTalkId(MERCHANT);
		addTalkId(MERCHANT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = null;
		switch (event)
		{
			case "package_deposit":
			{
				if (player.getAccountChars().size() < 1)
				{
					player.sendPacket(SystemMessageId.THAT_CHARACTER_DOES_NOT_EXIST);
				}
				else
				{
					player.sendPacket(new PackageToList(player.getAccountChars()));
				}
				break;
			}
			case "package_withdraw":
			{
				final PlayerFreight freight = player.getFreight();
				if ((freight != null) && (freight.getSize() > 0))
				{
					player.setActiveWarehouse(freight);
					for (Item i : player.getActiveWarehouse().getItems())
					{
						if (i.isTimeLimitedItem() && (i.getRemainingTime() <= 0))
						{
							player.getActiveWarehouse().destroyItem("ItemInstance", i, player, null);
						}
					}
					player.sendPacket(new WareHouseWithdrawalList(1, player, WareHouseWithdrawalList.FREIGHT));
					player.sendPacket(new WareHouseWithdrawalList(2, player, WareHouseWithdrawalList.FREIGHT));
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_HAVE_NOT_DEPOSITED_ANY_ITEMS_IN_YOUR_WAREHOUSE);
				}
				break;
			}
			case "back":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/32478.html")));
				break;
			}
			case "attendance_rewards":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/enhancement.html")));
				break;
			}
			case "shop":
			{
				MultisellData.getInstance().separateAndSend(ATTENDANCE_REWARD_MULTISELL, player, null, false);
				break;
			}
			// Bypass
			case "Chat_Enhancement":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/enhancement.html")));
				break;
			}
			case "Chat_Events":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/events.html")));
				break;
			}
			case "Chat_Items":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/items.html")));
				break;
			}
			case "Chat_RemoveAug":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/removeaug.html")));
				break;
			}
			case "Chat_SoulCrystals":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/soulcrystals.html")));
				break;
			}
			case "Chat_ItemConversion":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/itemconversion.html")));
				break;
			}
			case "Chat_TransferItem":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/transferitem.html")));
				break;
			}
			case "Chat_Redeem":
			{
				player.sendMessage("There are no more dimensional items to be found.");
				break;
			}
			case "Chat_Weapons":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/weapons.html")));
				break;
			}
			case "Chat_Armors":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/armors.html")));
				break;
			}
			case "Chat_Agathions":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/agathions.html")));
				break;
			}
			case "Chat_Soulshots":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/soulshots.html")));
				break;
			}
			case "Chat_Adventures_Mark":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/adventuremark.html")));
				break;
			}
			case "Chat_Dyes":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/dyes.html")));
				break;
			}
			// Actions
			case "removeAug":
			{
				player.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET);
				break;
			}
			case "insertSoulCrystals":
			{
				player.sendPacket(ExShowEnsoulWindow.STATIC_PACKET);
				break;
			}
			case "extractSoulCrystals":
			{
				player.sendPacket(ExShowEnsoulExtractionWindow.STATIC_PACKET);
				break;
			}
			case "items_conversion":
			{
				player.setTarget(player);
				player.sendPacket(new ExShowUpgradeSystem());
				break;
			}
			// Multisell
			case "Ex_BossWeapFragShop":
			{
				MultisellData.getInstance().separateAndSend(EX_BOSS_WEAPON_SHOP, player, null, false);
				break;
			}
			case "Ex_LaVieEnRoseShop":
			{
				MultisellData.getInstance().separateAndSend(EX_LA_VIE_EN_ROSE, player, null, false);
				break;
			}
			case "Ex_HeavyAGrade":
			{
				MultisellData.getInstance().separateAndSend(EX_HEAVY_A_GRADE, player, null, false);
				break;
			}
			case "Ex_LightAGrade":
			{
				MultisellData.getInstance().separateAndSend(EX_LIGHT_A_GRADE, player, null, false);
				break;
			}
			case "Ex_RobeAgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_ROBE_A_GRADE, player, null, false);
				break;
			}
			case "Ex_WeaponAgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_WEAPON_A_GRADE, player, null, false);
				break;
			}
			case "Ex_SpecialAgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_SPECIAL_A_GRADE, player, null, false);
				break;
			}
			case "Ex_HeavyBGrade":
			{
				MultisellData.getInstance().separateAndSend(EX_HEAVY_B_GRADE, player, null, false);
				break;
			}
			case "Ex_LightBGrade":
			{
				MultisellData.getInstance().separateAndSend(EX_LIGHT_B_GRADE, player, null, false);
				break;
			}
			case "Ex_RobeBgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_ROBE_B_GRADE, player, null, false);
				break;
			}
			case "Ex_WeaponBgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_WEAPON_B_GRADE, player, null, false);
				break;
			}
			case "Ex_Weapon7Bgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_WEAPON_7_B_GRADE, player, null, false);
				break;
			}
			case "Ex_WeaponCgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_WEAPON_C_GRADE, player, null, false);
				break;
			}
			case "Ex_ArmorCgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_ARMOR_C_GRADE, player, null, false);
				break;
			}
			case "Ex_Armor4Cgrade":
			{
				MultisellData.getInstance().separateAndSend(EX_ARMOR_4_C_GRADE, player, null, false);
				break;
			}
			case "Ex_AgathionSpirit":
			{
				MultisellData.getInstance().separateAndSend(EX_AGATHION_SPIRIT, player, null, false);
				break;
			}
			case "Ex_Soulshot":
			{
				MultisellData.getInstance().separateAndSend(EX_SOULSHOT, player, null, false);
				break;
			}
			case "Take_Adventures_Mark":
			{
				if ((player.getLevel() >= 20) || (player.getLevel() <= 30))
				{
					final long itemCount = getQuestItemsCount(player, ADVENTURER_MARK_LV_1);
					if (itemCount >= 1)
					{
						player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/items.html")));
						player.sendPacket(new NpcHtmlMessage(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/checkurinv.html")));
						return null;
					}
					
					player.addItem("GameAssistant", ADVENTURER_MARK_LV_1, 1, null, false);
				}
				if ((player.getLevel() >= 30) || (player.getLevel() <= 40))
				{
					final long itemCount = getQuestItemsCount(player, ADVENTURER_MARK_LV_2);
					if (itemCount >= 1)
					{
						player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/items.html")));
						player.sendPacket(new NpcHtmlMessage(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/checkurinv.html")));
						return null;
					}
					
					player.addItem("GameAssistant", ADVENTURER_MARK_LV_2, 1, null, false);
				}
				if ((player.getLevel() >= 40) || (player.getLevel() <= 60))
				{
					final long itemCount = getQuestItemsCount(player, ADVENTURER_MARK_LV_3);
					if (itemCount >= 1)
					{
						player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/items.html")));
						player.sendPacket(new NpcHtmlMessage(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/checkurinv.html")));
						return null;
					}
					
					player.addItem("GameAssistant", ADVENTURER_MARK_LV_3, 1, null, false);
				}
				if ((player.getLevel() >= 60) || (player.getLevel() <= 75))
				{
					final long itemCount = getQuestItemsCount(player, ADVENTURER_MARK_LV_4);
					if (itemCount >= 1)
					{
						player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/items.html")));
						player.sendPacket(new NpcHtmlMessage(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/checkurinv.html")));
						return null;
					}
					
					player.addItem("GameAssistant", ADVENTURER_MARK_LV_4, 1, null, false);
				}
				if ((player.getLevel() >= 76) || (player.getLevel() <= 99))
				{
					final long itemCount = getQuestItemsCount(player, ADVENTURER_MARK_LV_5);
					if (itemCount >= 1)
					{
						player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/items.html")));
						player.sendPacket(new NpcHtmlMessage(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/checkurinv.html")));
						return null;
					}
					
					player.addItem("GameAssistant", ADVENTURER_MARK_LV_5, 1, null, false);
				}
				break;
			}
			case "Ex_Dyes1":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/dyes.html")));
				MultisellData.getInstance().separateAndSend(EX_DYES1, player, null, false);
				break;
			}
			case "Ex_Dyes2":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/dyes.html")));
				MultisellData.getInstance().separateAndSend(EX_DYES2, player, null, false);
				break;
			}
			case "Ex_Dyes3":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/dyes.html")));
				MultisellData.getInstance().separateAndSend(EX_DYES3, player, null, false);
				break;
			}
			case "Ex_Dyes4":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/dyes.html")));
				MultisellData.getInstance().separateAndSend(EX_DYES4, player, null, false);
				break;
			}
			case "exc_black_sayha_cloak":
			{
				final long itemCount = getQuestItemsCount(player, SAYHA_CLOAK_COUPON);
				if (itemCount < 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				takeItems(player, SAYHA_CLOAK_COUPON, 1);
				giveItems(player, BLACK_SAYHA_CLOAK, 1);
				break;
			}
			case "exc_black_sayha_cloak_1":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 1) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 1);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(1);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_2":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 2) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 2)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 2);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(2);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_3":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 3) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 3)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 3);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(3);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_4":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 4) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 5)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 5);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(4);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_5":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 5) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 10)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 10);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(5);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_6":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 6) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 25)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 25);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(6);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_7":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 7) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 81)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 81);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(7);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_8":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 8) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 200)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 200);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(8);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_9":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 9) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 300)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 300);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(9);
				player.sendItemList();
				break;
			}
			case "exc_black_sayha_cloak_10":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 10) && (item.getId() == BLACK_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, BLACK_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 400)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 400);
				final Item reward = player.addItem("GameAssistant", BLACK_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(10);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak":
			{
				final long itemCount = getQuestItemsCount(player, SAYHA_CLOAK_COUPON);
				if (itemCount < 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				takeItems(player, SAYHA_CLOAK_COUPON, 1);
				giveItems(player, WHITE_SAYHA_CLOAK, 1);
				break;
			}
			case "exc_white_sayha_cloak_1":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 1) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 1);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(1);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_2":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 2) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 2)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 2);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(2);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_3":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 3) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 3)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 3);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(3);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_4":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 4) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 5)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 5);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(4);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_5":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 5) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 10)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 10);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(5);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_6":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 6) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 25)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 25);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(6);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_7":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 7) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 81)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 81);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(7);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_8":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 8) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 200)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 200);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(8);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_9":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 9) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 300)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 300);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(9);
				player.sendItemList();
				break;
			}
			case "exc_white_sayha_cloak_10":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 10) && (item.getId() == WHITE_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, WHITE_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 400)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 400);
				final Item reward = player.addItem("GameAssistant", WHITE_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(10);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak":
			{
				final long itemCount = getQuestItemsCount(player, SAYHA_CLOAK_COUPON);
				if (itemCount < 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				takeItems(player, SAYHA_CLOAK_COUPON, 1);
				giveItems(player, RED_SAYHA_CLOAK, 1);
				break;
			}
			case "exc_red_sayha_cloak_1":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 1) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 1);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(1);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_2":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 2) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 2)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 2);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(2);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_3":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 3) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 3)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 3);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(3);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_4":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 4) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 5)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 5);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(4);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_5":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 5) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 10)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 10);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(5);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_6":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 6) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 25)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 25);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(6);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_7":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 7) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 81)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 81);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(7);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_8":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 8) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 200)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 200);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(8);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_9":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 9) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 300)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 300);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(9);
				player.sendItemList();
				break;
			}
			case "exc_red_sayha_cloak_10":
			{
				final List<Item> cloaks = new ArrayList<>();
				for (Item item : player.getInventory().getItems())
				{
					if ((item.getEnchantLevel() == 10) && (item.getId() == RED_SAYHA_CLOAK))
					{
						cloaks.add(item);
					}
				}
				if (cloaks.isEmpty())
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final long itemCount = getQuestItemsCount(player, RED_SAYHA_CLOAK);
				if (itemCount > 1)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				final Item cloak = cloaks.stream().findFirst().get();
				final long packageCount = getQuestItemsCount(player, PACKAGE_CLOAK);
				if (packageCount < 400)
				{
					player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/no_cloak.html")));
					return null;
				}
				
				player.destroyItem("GameAssistant", cloak, player, true);
				takeItems(player, PACKAGE_CLOAK, 400);
				final Item reward = player.addItem("GameAssistant", RED_SAYHA_CLOAK, 1, null, false);
				reward.setEnchantLevel(10);
				player.sendItemList();
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/GameAssistant/32478.html")));
		return null;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_BYPASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerBypass(OnPlayerBypass event)
	{
		final Player player = event.getPlayer();
		if (event.getCommand().startsWith(COMMAND_BYPASS))
		{
			notifyEvent(event.getCommand().replace(COMMAND_BYPASS, ""), null, player);
		}
	}
	
	public static void main(String[] args)
	{
		new GameAssistant();
	}
}