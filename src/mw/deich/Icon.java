package mw.deich;

/**
 *	This is an interface for the Icon.
 * @author Marvin Wunderlich opidopiopi@gmail.com
 */
public interface Icon {
	/**
	 * Returns the Icons x coordinate.
	 * The position wraps over all screens.
	 * x == 0 means the Icon is on your left most monitor on the left edge.
	 * 
	 * @return the x coordinate
	 */
	public int getX();
	
	/**
	 * Returns the Icons y coordinate.
	 * The position wraps over all screens.
	 * y == 0 means the Icon is on your up most monitor on the upper edge.
	 * 
	 * @return the y coordinate
	 */
	public int getY();
	
	/**
	 * Sets the Icons x coordinate.
	 * The position wraps over all screens.
	 * x == 0 means the Icon is on your left most monitor on the left edge.
	 * 
	 * @param x		the new x coordinate
	 */
	public void setX(int x);
	
	/**
	 * Sets the Icons y coordinate.
	 * The position wraps over all screens.
	 * y == 0 means the Icon is on your up most monitor on the upper edge.
	 * 
	 * @param y		the new y coordinate
	 */
	public void setY(int y);
}
