package com.library.widget.wheel;


/**
 * Wheel changed listener interface.
 * <p>The onChanged() method is called whenever current wheel positions is changed:
 * <li> New Wheel position is set
 * <li> Wheel view is scrolled
 *
 * Created by chen_fulei on 2015/8/4.
 */
public interface OnWheelChangedListener {
	/**
	 * Callback method to be invoked when current item changed
	 * @param wheel the wheel view whose state has changed
	 * @param oldValue the old value of current item
	 * @param newValue the new value of current item
	 */
	void onChanged(WheelView wheel, int oldValue, int newValue);
}
