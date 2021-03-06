package cd4017be.lib.util;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictStack {
	
	public String id;
	public int stacksize;
	
	public OreDictStack(String id, int n) {
		this.id = id;
		this.stacksize = n;
	}
	
	public OreDictStack(int id, int n) {
		this.id = OreDictionary.getOreName(id);
		this.stacksize = n;
	}
	
	public static OreDictStack deserialize(String s) {
		int p = s.indexOf('*');
    	short n = 1;
    	if (p > 0) {
    		try {n = Short.parseShort(s.substring(0, p));} catch (NumberFormatException e){}
    		s = s.substring(p + 1);
    		if (n <= 0) n = 1;
    	}
    	if (s.isEmpty()) return null;
		return new OreDictStack(s, n);
	}
	
	public static OreDictStack[] get(ItemStack item) {
		if (item == null || item.getItem() == null) return null;
		int[] i = OreDictionary.getOreIDs(item);
		OreDictStack[] stacks = new OreDictStack[i.length];
		for (int j = 0; j < i.length; j++) stacks[j] = new OreDictStack(i[j], item.stackSize);
		return stacks;
	}
	
	public boolean isEqual(ItemStack item) {
		if (item == null || item.getItem() == null) return false;
		int ore = OreDictionary.getOreID(id);
		for (int id : OreDictionary.getOreIDs(item))
    		if (id == ore) return true;
		return false;
	}
	
	public ItemStack[] getItems() {
		List<ItemStack> list = OreDictionary.getOres(id);
		ItemStack[] items = new ItemStack[list.size()];
		int n = 0;
		for (ItemStack item : list) {
			items[n] = item.copy();
			items[n++].stackSize = stacksize;
		}
		return items;
	}

	public OreDictStack copy() {
		return new OreDictStack(id, stacksize);
	}
	
}
