package mw.deich;

import java.util.ArrayList;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 *	This is the IconHandler. You can get the number of Icons on the Desktop as well as set their position.
 *
 * @author Marvin Wunderlich opidopiopi@gmail.com
 */
/**
 * @author GOTT
 *
 */
public class IconHandler {
	private static HWND handler;
	
	private static final int LVM_GETITEMCOUNT = 0x1000 + 4;
	private static final int LVM_SETITEMPOSITION = 0x1000 + 15;
	private static final int LVM_GETITEMPOSITION = 0x1000 + 16;
	private static final int LVM_GETITEMSTATE = 0x1000 + 44;
	private static final int LVM_GETHOTITEM = 0x1000 + 61;
	
	private static final int VmOperation = 0x0008;
	private static final int VmRead = 0x0010;
	private static final int VmWrite = 0x0020;
	private static final int MEM_RESERVE = 0x2000;
	private static final int MEM_COMMIT = 0x1000;
	
	private static final int LVIS_FOCUSED = 0x0001;
	private static final int LVIS_SELECTED = 0x0002;
	private static final int LVIS_DROPHILITED = 0x0008;

	
	/**
	 * Instantiates a new IconHandler.
	 * @exception InstantiationError if for any reason the acquisition of the SysListView32 fails.
	 */
	public IconHandler() {
		HWND progMan = User32.INSTANCE.FindWindow("Progman", null);
		
		HWND shellDll_DefView = User32.INSTANCE.FindWindowEx(progMan, new HWND(Pointer.createConstant(0)), "SHELLDLL_DefView", null);
		handler = User32.INSTANCE.FindWindowEx(shellDll_DefView, new HWND(Pointer.createConstant(0)), "SysListView32", "FolderView");
		
		if(shellDll_DefView == null) {
			User32.INSTANCE.EnumWindows((hwnd, pntr) -> {
	            HWND p = User32.INSTANCE.FindWindowEx(hwnd, new HWND(Pointer.createConstant(0)), "SHELLDLL_DefView", null);

	            if (p != null) {
	            	handler = User32.INSTANCE.FindWindowEx(p, new HWND(Pointer.createConstant(0)), "SysListView32", "FolderView");
	            	return false;
	            }

	            return true;
	        }, Pointer.createConstant(0));
		}
		
		if(handler == null) {
			throw new InstantiationError("Failed to retrieve the SysListView32 handler!");
		}
	}
	
	/**
	 * Checks whether the icon is focused.
	 * 
	 * @param index	the index of the icon to be checked.
	 * @return	returns whether the icon is focused or not.
	 */
	public boolean isIconFocused(int index) {
		return (User32.INSTANCE.SendMessage(handler, LVM_GETITEMSTATE, new WPARAM(index), new LPARAM(LVIS_FOCUSED)).intValue() & LVIS_FOCUSED) > 0;
	}
	
	/**
	 * Checks whether the icon is selected.
	 * 
	 * @param index	the index of the icon to be checked.
	 * @return	returns whether the icon is selected or not.
	 */
	public boolean isIconSelected(int index) {
		return (User32.INSTANCE.SendMessage(handler, LVM_GETITEMSTATE, new WPARAM(index), new LPARAM(LVIS_SELECTED)).intValue() & LVIS_SELECTED) > 0;
	}
	
	/**
	 * Checks whether the icon is highlighted.
	 * 
	 * @param index	the index of the icon to be checked.
	 * @return	returns whether the icon is highlighted or not.
	 */
	public boolean isIconDropHilited(int index) {
		return (User32.INSTANCE.SendMessage(handler, LVM_GETITEMSTATE, new WPARAM(index), new LPARAM(LVIS_DROPHILITED)).intValue() & LVIS_DROPHILITED) > 0;
	}
	
	/**
	 * Returns the index of the selected icon or -1 if none selected;
	 * 
	 * @return	the index of the selected icon.
	 */
	public int getFocusedIconIndex() {
		int i = -1;
		
		for(int c = 0; c < getIconNum(); c++) {
			if(isIconFocused(c)) {
				i = c;
				break;
			}
		}
		
		return i;
	}
	
	/**
	 * Returns the indices of the selected icons;
	 * 
	 * @return	the indices of the selected icons.
	 */
	public int[] getSelectedIconsIndices() {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		
		for(int c = 0; c < getIconNum(); c++) {
			if(isIconSelected(c)) {
				indices.add(c);
			}
		}
		
		if(indices.size() == 0) indices.add(-1);
		
		return indices.stream().mapToInt(i -> i).toArray();
	}
	
	/**
	 * Returns the index of the for drag&drop highlighted icon or -1 if none selected;
	 * 
	 * @return	the index of the for drag&drop highlighted icon.
	 */
	public int getDropHilitedIconIndex() {
		int i = -1;
		
		for(int c = 0; c < getIconNum(); c++) {
			if(isIconDropHilited(c)) {
				i = c;
				break;
			}
		}
		
		return i;
	}
	
	/**
	 * Gets the index of the hot (the icon the mouse hovers over) desktop icon.
	 * 
	 * @return the index of the hot desktop icon or -1 when none selected
	 */
	public int getHotIconIndex() {
		return User32.INSTANCE.SendMessage(handler, LVM_GETHOTITEM, new WPARAM(0), new LPARAM(0)).intValue();
	}
	
	/**
	 * Gets the number of Icons on the Desktop.
	 * 
	 * @return the number of icons on the Desktop
	 */
	public int getIconNum() {
		return User32.INSTANCE.SendMessage(handler, LVM_GETITEMCOUNT, new WPARAM(0), new LPARAM(0)).intValue();
	}
	
	/**
	 * Sets the positions of the DesktopItems according to the Icons given as parameters.
	 * 
	 * @param icons		an Array of Icons which contain the new positions
	 */
	public void SetIconPositions(Icon[] icons)
    {
		if(icons.length > getIconNum()) {
			throw new IndexOutOfBoundsException("Length of icons[] is larger than the number of Icons!");
		}
		
		for (int i = 0; i < icons.length; i++) {
			User32.INSTANCE.SendMessage(handler, LVM_SETITEMPOSITION, new WPARAM(i), MakeLParam(icons[i].getX(), icons[i].getY()));
		}
    }
	
	/**
	 * Sets the positions of the desktop items according to the Icons given as parameters.
	 * If hotItemOff == true the position of the hot (the icon the mouse hovers over) item will not be updated.
	 * 
	 * @param icons		an array of icons which contain the new positions
	 */
	public void SetIconPositions(Icon[] icons, boolean hotItemOff)
    {
		if(icons.length > getIconNum()) {
			throw new IndexOutOfBoundsException("Length of icons[] is larger than the number of Icons!");
		}
		
		int hI = -1;
		if(hotItemOff) hI = getHotIconIndex();
		for (int i = 0; i < icons.length; i++) {
			if(!(hotItemOff && hI == i)) User32.INSTANCE.SendMessage(handler, LVM_SETITEMPOSITION, new WPARAM(i), MakeLParam(icons[i].getX(), icons[i].getY()));
		}
    }
	
	/**
	 * Sets the positions of the desktop items according to the Icons given as parameters.
	 * Will not update the icon with the given index doNotUpdate.
	 * 
	 * @param icons			an array of icons which contain the new positions
	 * @param doNotUpdate	the index of the icon not to be updated;
	 */
	public void SetIconPositions(Icon[] icons, int doNotUpdate)
    {
		if(icons.length > getIconNum()) {
			throw new IndexOutOfBoundsException("Length of icons[] is larger than the number of Icons!");
		}
		
		for (int i = 0; i < icons.length; i++) {
			if(i != doNotUpdate) User32.INSTANCE.SendMessage(handler, LVM_SETITEMPOSITION, new WPARAM(i), MakeLParam(icons[i].getX(), icons[i].getY()));
		}
    }
	
	/**
	 * Sets the position of the desktop item according to the icon given as parameter.
	 * 
	 * @param icon		the icon which contains the new position
	 */
	public void SetIconPosition(Icon icon, int index)
    {
		if(index > getIconNum() || index < 0) {
			throw new IndexOutOfBoundsException("Index out of bounds!");
		}
		
		User32.INSTANCE.SendMessage(handler, LVM_SETITEMPOSITION, new WPARAM(index), MakeLParam(icon.getX(), icon.getY()));
    }
	
	/**
	 * Constructs the LPARAM from the given x and y coordinates.
	 * 
	 * @param wLow	the x coordinate
	 * @param wHigh	the y coordinate
	 * @return		the LPARAM
	 */
	private LPARAM MakeLParam(int wLow, int wHigh)
    {
        return new LPARAM(((short)wHigh << 16) | (wLow & 0xffff));
    }
	
	/**
	 * Retrieves the icon positions and stores them in the given array.
	 * Note that the array has to be the size of the number of actual Items on the Desktop.
	 * 
	 * @param icons	the array of items
	 */
	public void getIconPositions(Icon[] icons) {
		IntByReference processID = new IntByReference(0);
		User32.INSTANCE.GetWindowThreadProcessId(handler, processID);
		
		HANDLE desktopProcessHandle = null;
		try {
			//create new process so we can get a sharedMemPointer with that
			desktopProcessHandle = Kernel32.INSTANCE.OpenProcess(VmOperation | VmRead | VmWrite, false, processID.getValue());
			
			getIconPositions(new HWND(desktopProcessHandle.getPointer()), icons);
		}finally {
			if(desktopProcessHandle != null) {
				Kernel32.INSTANCE.CloseHandle(desktopProcessHandle);
			}
		}
	}
	
	/**
	 * Uses the new process handler to get the sharedMemPointer.
	 * 
	 * @param desktopProcessHandle
	 * @param icons
	 */
	private void getIconPositions(HANDLE desktopProcessHandle, Icon[] icons) {
		int sharedMemPointer = 0;
		try {
			//request sharedMemPointer from windows
			sharedMemPointer = Kernel32Ex.INSTANCE.VirtualAllocEx(desktopProcessHandle, Pointer.createConstant(0), new BaseTSD.SIZE_T(4096), MEM_COMMIT | MEM_RESERVE, 0x04);
			
			getIconPositions(desktopProcessHandle, sharedMemPointer, icons);
		} finally {
			if(sharedMemPointer != 0) {
				Kernel32Ex.INSTANCE.VirtualFreeEx(desktopProcessHandle, new Pointer(sharedMemPointer), new BaseTSD.SIZE_T(0), 0x8000);
			}
		}
	}
	
	
	/**
	 * Uses the sharedMemPointer as an address for windows to store the position data.
	 * 
	 * @param desktopProcessHandle
	 * @param sharedMemPointer
	 * @param icons
	 */
	private void getIconPositions(HANDLE desktopProcessHandle, int sharedMemPointer, Icon[] icons) {
		for (int i = 0; i < icons.length; i++) {
			IntByReference numBytes = new IntByReference(0);	//The number of bytes written -> not actually used
			Memory mem = new Memory(8);							//The memory the positions are written to
			
			Kernel32.INSTANCE.WriteProcessMemory(desktopProcessHandle, new Pointer(sharedMemPointer), mem, 8, numBytes);
			
			//Let windows write the data at the address of the sharedMemPointer
			User32.INSTANCE.SendMessage(handler, LVM_GETITEMPOSITION, new WPARAM(i), new LPARAM(sharedMemPointer));
			
			//retrieve the data written
			Kernel32.INSTANCE.ReadProcessMemory(desktopProcessHandle, new Pointer(sharedMemPointer), mem, 8, numBytes);
			
			int x = mem.getInt(0);	//get first integer -> x
			int y = mem.getInt(4);	//get second integer -> y
			
			icons[i].setX(x);
			icons[i].setY(y);
		}
	}
	
	
	/**
	 * Interface to map the missing VirtualFreeEx and VirtualAllocEx.
	 *
	 */
	private interface Kernel32Ex extends StdCallLibrary 
    {
        public static final Kernel32Ex INSTANCE = (Kernel32Ex)Native.load("kernel32", Kernel32Ex.class, W32APIOptions.UNICODE_OPTIONS);

        boolean VirtualFreeEx(HANDLE hProcess, Pointer lpAddress, SIZE_T dwSize, int freeType);
        int VirtualAllocEx(HANDLE hProcess, Pointer lpAddress, SIZE_T dwSize, int flAllocationType, int flProtect);
    }
}
