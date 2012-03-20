package org.racenet.models;

import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class UserListAdapter implements ListAdapter {
	
	private Context context;
	private List<UserItem> users;
	
	public UserListAdapter(Context c) {
		
		context = c;
		users = new Database(context).getAllUsers();
	}

	public int getCount() {

		return users.size();
	}

	public Object getItem(int pos) {
		
		return users.get(pos).getName();
	}

	public long getItemId(int pos) {
		
		return users.get(pos).getId();
	}

	public int getItemViewType(int arg0) {
		
		return arg0;
	}

	public View getView(int arg0, View arg1, ViewGroup arg2) {
		
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
        TextView textView = new TextView(context);
        textView.setLayoutParams(lp);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        textView.setPadding(64, 0, 0, 0);
        textView.setText(Html.fromHtml(getItem(arg0).toString()));
        textView.setTextSize(32);

        return textView;
	}

	public int getViewTypeCount() {
		
		return 1;
	}

	public boolean hasStableIds() {
		
		return true;
	}

	public boolean isEmpty() {
		
		return users.size() == 0;
	}

	public void registerDataSetObserver(DataSetObserver arg0) {
		
	}

	public void unregisterDataSetObserver(DataSetObserver arg0) {
		
	}

	public boolean areAllItemsEnabled() {

		return true;
	}

	public boolean isEnabled(int arg0) {

		return true;
	}
}