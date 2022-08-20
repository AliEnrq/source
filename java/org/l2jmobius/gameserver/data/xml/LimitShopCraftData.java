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
package org.l2jmobius.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.l2jmobius.commons.util.IXmlReader;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.holders.LimitShopProductHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;

/**
 * @author Mobius, GustavoFonseca
 */
public class LimitShopCraftData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(LimitShopData.class.getName());
	
	private final List<LimitShopProductHolder> _products = new ArrayList<>();
	
	protected LimitShopCraftData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		_products.clear();
		parseDatapackFile("data/LimitShopCraft.xml");
		
		if (!_products.isEmpty())
		{
			LOGGER.info(getClass().getSimpleName() + ": Loaded " + _products.size() + " items.");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": System is disabled.");
		}
	}
	
	@Override
	public void parseDocument(Document doc, File f)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				final NamedNodeMap at = n.getAttributes();
				final Node attribute = at.getNamedItem("enabled");
				if ((attribute != null) && Boolean.parseBoolean(attribute.getNodeValue()))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("product".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							Node att;
							final StatSet set = new StatSet();
							for (int i = 0; i < attrs.getLength(); i++)
							{
								att = attrs.item(i);
								set.set(att.getNodeName(), att.getNodeValue());
							}
							
							final int id = parseInteger(attrs, "id");
							final int category = parseInteger(attrs, "category");
							final int minLevel = parseInteger(attrs, "minLevel", 1);
							final int maxLevel = parseInteger(attrs, "maxLevel", 999);
							final int[] ingredientIds = new int[5];
							ingredientIds[0] = 0;
							ingredientIds[1] = 0;
							ingredientIds[2] = 0;
							ingredientIds[3] = 0;
							ingredientIds[4] = 0;
							final long[] ingredientQuantities = new long[5];
							ingredientQuantities[0] = 0;
							ingredientQuantities[1] = 0;
							ingredientQuantities[2] = 0;
							ingredientQuantities[3] = 0;
							ingredientQuantities[4] = 0;
							final int[] ingredientEnchants = new int[5];
							ingredientEnchants[0] = 0;
							ingredientEnchants[1] = 0;
							ingredientEnchants[2] = 0;
							ingredientEnchants[3] = 0;
							ingredientEnchants[4] = 0;
							int productionId = 0;
							int productionId2 = 0;
							int productionId3 = 0;
							int productionId4 = 0;
							int productionId5 = 0;
							long count = 1L;
							long count2 = 1L;
							long count3 = 1L;
							long count4 = 1L;
							long count5 = 1L;
							float chance = 100f;
							float chance2 = 100f;
							float chance3 = 100f;
							float chance4 = 100f;
							int accountDailyLimit = 0;
							int accountBuyLimit = 0;
							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								attrs = b.getAttributes();
								
								if ("ingredient".equalsIgnoreCase(b.getNodeName()))
								{
									final int ingredientId = parseInteger(attrs, "id");
									final long ingredientQuantity = parseLong(attrs, "count", 1L);
									final int ingredientEnchant = parseInteger(attrs, "enchant", 0);
									
									final ItemTemplate item = ItemTable.getInstance().getTemplate(ingredientId);
									if (item == null)
									{
										LOGGER.severe(getClass().getSimpleName() + ": Item template null for itemId: " + productionId + " productId: " + id);
										continue;
									}
									
									if (ingredientIds[0] == 0)
									{
										ingredientIds[0] = ingredientId;
									}
									else if (ingredientIds[1] == 0)
									{
										ingredientIds[1] = ingredientId;
									}
									else if (ingredientIds[2] == 0)
									{
										ingredientIds[2] = ingredientId;
									}
									else if (ingredientIds[3] == 0)
									{
										ingredientIds[3] = ingredientId;
									}
									else
									{
										ingredientIds[4] = ingredientId;
									}
									
									if (ingredientQuantities[0] == 0)
									{
										ingredientQuantities[0] = ingredientQuantity;
									}
									else if (ingredientQuantities[1] == 0)
									{
										ingredientQuantities[1] = ingredientQuantity;
									}
									else if (ingredientQuantities[2] == 0)
									{
										ingredientQuantities[2] = ingredientQuantity;
									}
									else if (ingredientQuantities[3] == 0)
									{
										ingredientQuantities[3] = ingredientQuantity;
									}
									else
									{
										ingredientQuantities[4] = ingredientQuantity;
									}
									
									if (ingredientEnchants[0] == 0)
									{
										ingredientEnchants[0] = ingredientEnchant;
									}
									else if (ingredientEnchants[1] == 0)
									{
										ingredientEnchants[1] = ingredientEnchant;
									}
									else if (ingredientEnchants[2] == 0)
									{
										ingredientEnchants[2] = ingredientEnchant;
									}
									else if (ingredientEnchants[3] == 0)
									{
										ingredientEnchants[3] = ingredientEnchant;
									}
									else
									{
										ingredientEnchants[4] = ingredientEnchant;
									}
								}
								else if ("production".equalsIgnoreCase(b.getNodeName()))
								{
									productionId = parseInteger(attrs, "id");
									count = parseLong(attrs, "count", 1L);
									chance = parseFloat(attrs, "chance", 100f);
									productionId2 = parseInteger(attrs, "id2", 0);
									count2 = parseLong(attrs, "count2", 1L);
									chance2 = parseFloat(attrs, "chance2", 100f);
									productionId3 = parseInteger(attrs, "id3", 0);
									count3 = parseLong(attrs, "count3", 1L);
									chance3 = parseFloat(attrs, "chance3", 100f);
									productionId4 = parseInteger(attrs, "id4", 0);
									count4 = parseLong(attrs, "count4", 1L);
									chance4 = parseFloat(attrs, "chance4", 100f);
									productionId5 = parseInteger(attrs, "id5", 0);
									count5 = parseLong(attrs, "count5", 1L);
									accountDailyLimit = parseInteger(attrs, "accountDailyLimit", 0);
									accountBuyLimit = parseInteger(attrs, "accountBuyLimit", 0);
									
									final ItemTemplate item = ItemTable.getInstance().getTemplate(productionId);
									if (item == null)
									{
										LOGGER.severe(getClass().getSimpleName() + ": Item template null for itemId: " + productionId + " productId: " + id);
										continue;
									}
								}
							}
							
							_products.add(new LimitShopProductHolder(id, category, minLevel, maxLevel, ingredientIds, ingredientQuantities, ingredientEnchants, productionId, count, chance, productionId2, count2, chance2, productionId3, count3, chance3, productionId4, count4, chance4, productionId5, count5, accountDailyLimit, accountBuyLimit));
						}
					}
				}
			}
		}
	}
	
	public LimitShopProductHolder getProduct(int id)
	{
		for (LimitShopProductHolder product : _products)
		{
			if (product.getId() == id)
			{
				return product;
			}
		}
		return null;
	}
	
	public Collection<LimitShopProductHolder> getProducts()
	{
		return _products;
	}
	
	public static LimitShopCraftData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LimitShopCraftData INSTANCE = new LimitShopCraftData();
	}
}