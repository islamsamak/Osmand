package net.osmand.plus.mapcontextmenu.other;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.osmand.plus.IconsCache;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.mapcontextmenu.other.ObjectSelectionMenu.MenuObject;

import java.util.List;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class ObjectSelectionMenuFragment extends Fragment implements AdapterView.OnItemClickListener {
	public static final String TAG = "ObjectSelectionMenuFragment";

	private ArrayAdapter<MenuObject> listAdapter;
	private ObjectSelectionMenu menu;

	public ObjectSelectionMenu getMenu() {
		return menu;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null && getActivity() instanceof MapActivity) {
			menu = ObjectSelectionMenu.restoreMenu(savedInstanceState, (MapActivity) getActivity());
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.menu_obj_selection_fragment, container, false);

		ListView listView = (ListView) view.findViewById(R.id.list);
		listAdapter = createAdapter();
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(this);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		menu.getMapActivity().getContextMenu().setBaseFragmentVisibility(false);
	}

	@Override
	public void onStop() {
		super.onStop();
		menu.onDismiss();
		menu.getMapActivity().getContextMenu().setBaseFragmentVisibility(true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		menu.saveMenu(outState);
	}

	public static void showInstance(ObjectSelectionMenu menu) {
		int slideInAnim = menu.getSlideInAnimation();
		int slideOutAnim = menu.getSlideOutAnimation();

		ObjectSelectionMenuFragment fragment = new ObjectSelectionMenuFragment();
		fragment.menu = menu;
		menu.getMapActivity().getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(slideInAnim, slideOutAnim, slideInAnim, slideOutAnim)
				.add(R.id.fragmentContainer, fragment, TAG)
				.addToBackStack(TAG).commit();
	}

	private ArrayAdapter<MenuObject> createAdapter() {
		final List<MenuObject> items = menu.getObjects();
		return new ArrayAdapter<MenuObject>(menu.getMapActivity(), R.layout.menu_obj_list_item, items) {

			@SuppressLint("InflateParams")
			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					v = menu.getMapActivity().getLayoutInflater().inflate(R.layout.menu_obj_list_item, null);
				}
				final MenuObject item = getItem(position);
				buildHeader(v, item, menu.getMapActivity());
				return v;
			}
		};
	}

	private void buildHeader(View view, MenuObject item, MapActivity mapActivity) {

		IconsCache iconsCache = mapActivity.getMyApplication().getIconsCache();
		final View iconLayout = view.findViewById(R.id.context_menu_icon_layout);
		final ImageView iconView = (ImageView) view.findViewById(R.id.context_menu_icon_view);
		Drawable icon = item.getLeftIcon();
		int iconId = item.getLeftIconId();
		if (icon != null) {
			iconView.setImageDrawable(icon);
			iconLayout.setVisibility(View.VISIBLE);
		} else if (iconId != 0) {
			iconView.setImageDrawable(iconsCache.getIcon(iconId,
					menu.isLight() ? R.color.osmand_orange : R.color.osmand_orange_dark, 0.75f));
			iconLayout.setVisibility(View.VISIBLE);
		} else {
			iconLayout.setVisibility(View.GONE);
		}

		// Text line 1
		TextView line1 = (TextView) view.findViewById(R.id.context_menu_line1);
		line1.setText(item.getTitleStr());

		// Text line 2
		TextView line2 = (TextView) view.findViewById(R.id.context_menu_line2);
		line2.setText(item.getLocationStr());
		Drawable slIcon = item.getSecondLineIcon();
		if (slIcon != null) {
			line2.setCompoundDrawablesWithIntrinsicBounds(slIcon, null, null, null);
			line2.setCompoundDrawablePadding(dpToPx(5f));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		menu.openContextMenu(listAdapter.getItem(position));
	}

	public void dismissMenu() {
		if (menu.getMapActivity().getContextMenu().isVisible()) {
			menu.getMapActivity().getContextMenu().hide();
		} else {
			menu.getMapActivity().getSupportFragmentManager().popBackStack();
		}
	}

	private int dpToPx(float dp) {
		Resources r = getActivity().getResources();
		return (int) TypedValue.applyDimension(
				COMPLEX_UNIT_DIP,
				dp,
				r.getDisplayMetrics()
		);
	}
}