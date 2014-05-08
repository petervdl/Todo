// initial exercise, Peter van der Linden, pvdl@afu.com, 
// a simple todo list application for CodePath
// May 5 2014

// have enhanced this app:
// * hide the soft keyboard when returning to main Activity after editing
// * fixed the bug where you need to save the files on adds, as well as deletes.
// * added Commons IO library for better IO from http://commons.apache.org/proper/commons-io 

// ideas to enhance this app:
// * (Top of the list) make it more graceful, when list starts to encroach on "Add" field!
// * write in smaller steps than "write entire file when anything added/deleted"
//   the simplest way to do that, is probably to store list items in an sql database
// * add a check off mark to each item, that can be set/unset
// * maintain a count of number of items in the list, in the Action Bar
// * resolve the logged issue in EditItemActivity.  You get this sometimes when deleting chars in the list item you're editing.
//   it doesn't affect the app running, but it would be good to tie up loose end
//   05-07 10:01:24.758: E/SpannableStringBuilder(23869): SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length
//   05-07 10:01:25.509: W/InputEventReceiver(23869): Attempted to finish an input event but the input event receiver has already been disposed.
//   05-07 10:01:25.509: W/InputEventReceiver(23869): Attempted to finish an input event but the input event receiver has already been disposed.
//   05-07 10:01:25.509: W/ViewRootImpl(23869): Dropping event due to root view being removed: MotionEvent { action=ACTION_MOVE, id[0]=0, x[0]=1003.0, y[0]=1202.0, toolType[0]=TOOL_TYPE_FINGER, buttonState=0, metaState=0, flags=0x0, edgeFlags=0x0, pointerCount=1, historySize=0, eventTime=134558309, downTime=134558297, deviceId=20, source=0x1002 }
//   stackoverflow suggests it is due to using a non-Google soft keyboard.  But I am not doing that.

package com.afu.todo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

// See assignments descriptions at
//   https://www.dropbox.com/s/ar0rmavbutnit81/(Preface%20B)%20Android%20Quickstart.pdf
//   https://gist.github.com/nesquena/843228e83fdc4f5ddc4e

public class MainActivity extends Activity {
	private ArrayList<String> items;
	private ArrayAdapter<String> itemsAdapter;
	private ListView lvItems;
	private Context ctx;
	private int savedPos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	    ctx = getBaseContext();  // need this for Intent later

		readItems();
		lvItems = (ListView) findViewById(R.id.lvItems);

		itemsAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, items);
		lvItems.setAdapter(itemsAdapter);
		setupListViewListener();
	}
	
	public void addTodoItem(View v) {
		EditText etNewItem = (EditText)
				findViewById(R.id.etNewItem);
		itemsAdapter.add(etNewItem.getText().toString());
		etNewItem.setText("");
        saveItems();     // need to write file on Add, as well as on Delete
	}
	
	private void setupListViewListener() {
		lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			// a long click means we must delete this item from list
			@Override
			public boolean onItemLongClick(AdapterView<?> aview,
					View item, int pos, long id) {
				items.remove(pos);
				itemsAdapter.notifyDataSetChanged();
                saveItems();
				return true;
			}
		});
		
		lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			// a short click means we need to start the editItem activity
			@Override
			public void onItemClick(AdapterView<?> aview, View item, int pos, long id) {
				
				savedPos = pos;  // keep this around, to later put updated String value at this position
		    	Intent i = new Intent(ctx, EditItemActivity.class);

				// get the string from the clicked item, put into extra
				String s = ((TextView) item).getText().toString();
				i.putExtra("editMe", s);
				i.putExtra("position", pos);
				
				startActivityForResult(i, 0);
				return;
			}
		});

	}
	

	private void readItems() {
		File filesDir = getFilesDir();
		File todoFile = new File(filesDir, "todo.txt");
		try {
			items = new ArrayList<String>(FileUtils.readLines(todoFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveItems() {
		File filesDir = getFilesDir();
		File todoFile = new File(filesDir, "todo.txt");
		try {
			FileUtils.writeLines(todoFile, items);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// now we finished editing, get rid of keyboard (Got this from stackoverflow.com)
		// http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		if (resultCode == RESULT_OK) {
			// Extract name value from result extras
			String name = data.getExtras().getString("edited");
			
			items.set(savedPos, name);
			itemsAdapter.notifyDataSetChanged();

			saveItems();
		}
	} 

}