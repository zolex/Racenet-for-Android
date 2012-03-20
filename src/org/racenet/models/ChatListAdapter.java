package org.racenet.models;

import java.util.List;

import org.racenet.R;

import android.content.Context;
import android.database.DataSetObserver;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ChatListAdapter implements ListAdapter {
	
	private Context context;
	private List<ChatItem> messages;
	
	public ChatListAdapter(Context c, int userId) {
		
		context = c;
		messages = new Database(context).getMessages(userId);
	}

	public int getCount() {

		return messages.size();
	}

	public Object getItem(int pos) {
		
		return messages.get(pos);
	}

	public long getItemId(int pos) {
		
		return messages.get(pos).getId();
	}

	public int getItemViewType(int arg0) {
		
		return arg0;
	}
	
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		
		ChatItem item = (ChatItem)getItem(arg0);
		TextView textView =  (TextView)View.inflate(context, R.layout.chatitem, null);
		textView.setText(Html.fromHtml("<b>" + item.getName() + ":</b> " + item.getText()));

        return textView;
	}

	public int getViewTypeCount() {
		
		return 1;
	}

	public boolean hasStableIds() {
		
		return true;
	}

	public boolean isEmpty() {
		
		return messages.size() == 0;
	}

	public void registerDataSetObserver(DataSetObserver arg0) {
		
	}

	public void unregisterDataSetObserver(DataSetObserver arg0) {
		
	}

	public boolean areAllItemsEnabled() {

		return false;
	}

	public boolean isEnabled(int arg0) {

		return false;
	}
}