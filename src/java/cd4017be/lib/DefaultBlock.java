/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.lib;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.StatCollector;

/**
 *
 * @author CD4017BE
 */
public class DefaultBlock extends Block
{
    
    public DefaultBlock(String id, Material m, Class<? extends ItemBlock> item)
    {
        super(m);
        this.setCreativeTab(CreativeTabs.tabBlock);
        this.setUnlocalizedName(id);
        BlockItemRegistry.registerBlock(this, id, item);
    }

	@Override
	public String getLocalizedName() 
	{
		return StatCollector.translateToLocal(this.getUnlocalizedName().replaceFirst("tile.", "tile.cd4017be.") + ".name");
	}
    
}
