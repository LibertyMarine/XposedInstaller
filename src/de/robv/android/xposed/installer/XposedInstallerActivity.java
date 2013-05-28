package de.robv.android.xposed.installer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.SimpleAdapter;

public class XposedInstallerActivity extends Activity {
	static final String TAG = "xposed_installer";
	
	static final String EXTRA_OPEN_TAB = "opentab";
	static final int TAB_INSTALL = 0;
	static final int TAB_MODULES = 1;
	static final int TAB_DOWNLOAD = 2;

	static final int NOTIFICATION_MODULE_NOT_ACTIVATED_YET = 1;
	
	int currentNavItem = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancelAll();
        
		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		bar.setDisplayShowTitleEnabled(false);
		bar.setDisplayHomeAsUpEnabled(true);

		final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		data.add(makeNavigationItem(getString(R.string.tabInstall), InstallerFragment.class));
		data.add(makeNavigationItem(getString(R.string.tabModules), ModulesFragment.class));
		data.add(makeNavigationItem(getString(R.string.tabDownload), DownloadFragment.class));

		SimpleAdapter adapter = new SimpleAdapter(this, data,
				android.R.layout.simple_spinner_dropdown_item,
				new String[] { "title" },
				new int[] { android.R.id.text1 });

		bar.setListNavigationCallbacks(adapter, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				if (currentNavItem == itemPosition)
					return true;

				Map<String, Object> map = data.get(itemPosition);

				Fragment fragment = (Fragment) map.get("fragment");
				if (fragment == null) {
					String fragmentClass = (String) map.get("fragment_class");
					fragment = Fragment.instantiate(XposedInstallerActivity.this, fragmentClass);
					map.put("fragment", fragment);
				}

				FragmentTransaction tx = getFragmentManager().beginTransaction();
				tx.replace(android.R.id.content, fragment);
				if (currentNavItem != -1)
					tx.addToBackStack(null);
				currentNavItem = itemPosition;
				tx.commit();

				return true;
			}
		});

        int selectTabIndex = -1; 
        if (getIntent().hasExtra(EXTRA_OPEN_TAB)) {
        	bar.setSelectedNavigationItem(getIntent().getIntExtra(EXTRA_OPEN_TAB, 0));
        	Object extra = getIntent().getExtras().get(EXTRA_OPEN_TAB);
        	try {
        		if (extra != null)
        			selectTabIndex = (Integer) extra;
        	} catch (ClassCastException e) {
        		String extraS = extra.toString();
        		if (extraS.equals("install"))
        			selectTabIndex = TAB_INSTALL;
        		else if (extraS.equals("modules")) 
        			selectTabIndex = TAB_MODULES;
        		else if (extraS.equals("download"))
        			selectTabIndex = TAB_DOWNLOAD;
        	}
        } else if (savedInstanceState != null) {
        	selectTabIndex = savedInstanceState.getInt("tab", -1);
        }
        
        if (selectTabIndex >= 0 && selectTabIndex < bar.getNavigationItemCount())
    		bar.setSelectedNavigationItem(getIntent().getIntExtra(EXTRA_OPEN_TAB, 0));
    }
    
	private Map<String, Object> makeNavigationItem(String title, Class<? extends Fragment> fragmentClass) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("title", title);
		map.put("fragment_class", fragmentClass.getName());
		return map;
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }

	void setNavItem(int position) {
		currentNavItem = position;
		getActionBar().setSelectedNavigationItem(position);
	}
}
