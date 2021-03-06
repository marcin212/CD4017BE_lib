/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cd4017be.api.energy;

import java.util.List;

import cd4017be.api.automation.IEnergy;
import cd4017be.api.automation.PipeEnergy;
import cd4017be.api.energy.EnergyAPI.IEnergyAccess;
import cd4017be.api.energy.EnergyAPI.IEnergyHandler;
import cd4017be.lib.TooltipInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 *
 * @author CD4017BE
 */
public class EnergyAutomation implements IEnergyHandler
{
	
	public static interface IEnergyItem {
		public int getEnergyCap(ItemStack item);
		public int getChargeSpeed(ItemStack item);
		public String getEnergyTag();
	}
	
	public static class EnergyItem implements IEnergyAccess {
		private final ItemStack stack;
		public final IEnergyItem item;
		/** [kJ] remaining fraction for use with precision mode
		 */
		public double fractal = 0;
		
		public EnergyItem(ItemStack stack, IEnergyItem item) {
			this.stack = stack;
			this.item = item;
			if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		}
		
		public void addInformation(List list) {
			list.add(String.format("Energy: %d / %d %s", this.getStorageI(), item.getEnergyCap(stack), TooltipInfo.getEnergyUnit()));
		}
		/**
		 * Get Integer tag value
		 * @return [kJ] stored energy
		 */
		public int getStorageI() {
			return stack.getTagCompound().getInteger(item.getEnergyTag());
		}
		/**
		 * Add Energy directly to the Integer tag
		 * @param n [kJ] amount
		 * @param s "access side" -1 = unlimited, 0 = limited
		 * @return [kJ] actually added energy
		 */
		public int addEnergyI(int n, int s) {
			if (n == 0) return n;
			int cap = item.getEnergyCap(stack);
			if (s >= 0) {
				int max = item.getChargeSpeed(stack);
				if (n > max) n = max;
				else if (n < -max) n = -max;
			}
			int e = stack.getTagCompound().getInteger(item.getEnergyTag()) + n;
			if (e < 0) {
				n -= e;
				e = 0;
			} else if (e > cap) {
				n -= e - cap;
				e = cap;
			}
			stack.getTagCompound().setInteger(item.getEnergyTag(), e);
			return n;
		}
		/**
		 * @param s "access side" -2 = precision using remain, else = default 
		 * @return [J] stored energy
		 */
		@Override
		public double getStorage(int s) {
			if (s == -2) return ((double)this.getStorageI() + fractal) * 1000D;
			else return (double)this.getStorageI() * 1000D;
		}
		/**
		 * @param s will be ignored
		 * @return [J] energy storage capacity
		 */
		@Override
		public double getCapacity(int s) {
			return item.getEnergyCap(stack) * 1000D;
		}
		/**
		 * @param E [J] energy to add
		 * @param s "access side" -1 = unlimited, 0 = limited, -2 = precision using remain 
		 * @return [J] actually added energy
		 */
		@Override
		public double addEnergy(double E, int s) {
			E /= 1000D;
			if (s == -2) {
				fractal = E - this.addEnergyI((int)Math.floor(E + fractal), s); 
				if (fractal < 0 || fractal >= 1) {
					double d = Math.floor(fractal);
					fractal -= d;
					E -= d;
				}
				return E * 1000D;
			} else return (double)this.addEnergyI(E < 0 ? (int)Math.ceil(E) : (int)Math.floor(E), s) * 1000D;
		}
	}
	
	public static class Cable implements IEnergyAccess {
		private final IEnergy energy;
		public Cable(IEnergy e) {
			this.energy = e;
		}
		
		@Override
		public double getStorage(int s) {
			PipeEnergy pipe = energy.getEnergy((byte)s);
			return pipe == null ? 0 : pipe.Ucap * pipe.Ucap;
		}
		
		@Override
		public double getCapacity(int s) {
			PipeEnergy pipe = energy.getEnergy((byte)s);
			return pipe == null ? 0 : (double)pipe.Umax * (double)pipe.Umax;
		}
		
		@Override
		public double addEnergy(double e, int s) {
			PipeEnergy pipe = energy.getEnergy((byte)s);
			if (pipe == null || pipe == PipeEnergy.empty) return 0;
			double d = pipe.Ucap * pipe.Ucap;
			double m = (double)pipe.Umax * (double)pipe.Umax;
			if (d + e < 0) {
				e = -d;
				pipe.Ucap = 0;
			} else if (d + e > m) {
				e = m - d;
				pipe.Ucap = pipe.Umax;
			} else pipe.addEnergy(e);
			return e;
		}
	}
	
	@Override
	public IEnergyAccess create(TileEntity te)  {
		return te instanceof IEnergy ? new Cable((IEnergy)te) : null;
	}
	
	@Override
	public IEnergyAccess create(ItemStack item) {
		return item != null && item.getItem() instanceof IEnergyItem ? new EnergyItem(item, (IEnergyItem)item.getItem()) : null;
	}
}
