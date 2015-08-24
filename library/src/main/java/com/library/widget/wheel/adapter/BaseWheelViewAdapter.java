package com.library.widget.wheel.adapter;

import java.util.LinkedList;
import java.util.List;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

/**
 * Abstract Wheel adapter.
 *
 * Created by chen_fulei on 2015/8/4.
 */
public abstract class BaseWheelViewAdapter implements WheelViewAdapter {
	// Observers
	private List<DataSetObserver> datasetObservers;

	@Override
	public abstract int getCount();
	
	@Override
	public abstract Object getItem(int position);

	@Override
	public abstract long getItemId(int position);
	
	@Override
	public abstract View getView(int index, View convertView, ViewGroup parent);
	
	@Override
	public abstract View getEmptyItem(View convertView, ViewGroup parent);

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		if (datasetObservers == null) {
			datasetObservers = new LinkedList<DataSetObserver>();
		}
		datasetObservers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (datasetObservers != null) {
			datasetObservers.remove(observer);
		}
	}

	/**
	 * Notifies observers about data changing
	 */
	protected void notifyDataChangedEvent() {
		if (datasetObservers != null) {
			for (DataSetObserver observer : datasetObservers) {
				observer.onChanged();
			}
		}
	}

	/**
	 * Notifies observers about invalidating data
	 */
	protected void notifyDataInvalidatedEvent() {
		if (datasetObservers != null) {
			for (DataSetObserver observer : datasetObservers) {
				observer.onInvalidated();
			}
		}
	}

	
}
