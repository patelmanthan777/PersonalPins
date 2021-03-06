package ch.ralena.personalpins.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

import ch.ralena.personalpins.FullScreenImageActivity;
import ch.ralena.personalpins.MainActivity;
import ch.ralena.personalpins.R;
import ch.ralena.personalpins.objects.Pin;
import ch.ralena.personalpins.objects.Tag;
import io.realm.Realm;

import static ch.ralena.personalpins.R.id.tagTitle;

public class PinDetailFragment extends Fragment {
	private Realm realm;
	private Pin pin;
	private MainActivity mainActivity;
	private LinearLayout tagLayout;
	private ImageView thumbnailPhoto;
	private VideoView thumbnailVideo;
	private ImageView thumbnailVideoPlay;
	private TextView titleText, noteText;
	private Button deleteButton;
	private HashMap<MenuItem, TagView> itemMap;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// remove titlebar
		mainActivity = (MainActivity) getActivity();
		mainActivity.hideActionBar();

		// get realm and pin object
		realm = Realm.getDefaultInstance();
		String id = getArguments().getString(PinsFragment.EXTRA_PIN_ID);
		pin = realm.where(Pin.class).equalTo("id", id).findFirst();

		View view = inflater.inflate(R.layout.fragment_pin_detail, container, false);
		tagLayout = (LinearLayout) view.findViewById(R.id.tagLayout);
		titleText = (TextView) view.findViewById(R.id.titleText);
		noteText = (TextView) view.findViewById(R.id.noteText);
		deleteButton = (Button) view.findViewById(R.id.deleteButton);
		thumbnailPhoto = (ImageView) view.findViewById(R.id.thumbnailPhoto);
		thumbnailVideo = (VideoView) view.findViewById(R.id.thumbnailVideo);
		thumbnailVideoPlay = (ImageView) view.findViewById(R.id.thumbnailVideoPlay);

		titleText.setText(pin.getTitle());
		noteText.setText(pin.getNote());

		setupDeleteButton(id);
		loadTags();
		loadThumbnail();

		return view;
	}

	private void loadThumbnail() {
		if (pin.getType().equals(Pin.TYPE_VIDEO)) {
			thumbnailVideo.setVisibility(View.VISIBLE);
			thumbnailVideoPlay.setVisibility(View.VISIBLE);
			thumbnailVideo.setVideoURI(Uri.parse(pin.getFilepath()));

			// set up media controller
			MediaController mediaController = new MediaController(getContext());
			mediaController.setAnchorView(thumbnailVideo);
			thumbnailVideo.setMediaController(mediaController);

			thumbnailVideo.setOnTouchListener((v, event) -> {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					thumbnailVideoPlay.setVisibility(View.INVISIBLE);
					thumbnailVideo.start();
					return true;
				}
				return false;
			});
			thumbnailVideo.setOnCompletionListener(mp -> thumbnailVideoPlay.setVisibility(View.VISIBLE));
		} else if (pin.getType().equals(Pin.TYPE_PICTURE)) {
			thumbnailPhoto.setOnClickListener(v -> {
				Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
				intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_FILEPATH, pin.getFilepath());

				ActivityOptionsCompat options =
						ActivityOptionsCompat.makeSceneTransitionAnimation(
								getActivity(),
								thumbnailPhoto,
								getString(R.string.image_transition));
				ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
			});
			thumbnailPhoto.setVisibility(View.VISIBLE);
			Uri imageUri = Uri.fromFile(new File(pin.getFilepath()));
			Picasso.with(getContext())
					.load(imageUri)
					.fit()
					.centerCrop()
					.into(thumbnailPhoto);
		}

	}

	private void setupDeleteButton(String id) {
		deleteButton.setOnClickListener(v -> new AlertDialog.Builder(getContext())
				.setMessage("Are you sure you want to delete this pin?")
				.setPositiveButton("Yes", (dialog, which) -> {
					realm.executeTransaction(r -> r
							.where(Pin.class)
							.equalTo("id", id)
							.findFirst()
							.deleteFromRealm());
					getFragmentManager().popBackStack();
				})
				.setNegativeButton("No", (dialog, which) -> dialog.cancel())
				.create()
				.show()
		);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mainActivity.showActionBar();
		if (realm != null) {
			realm.close();
			realm = null;
		}
	}

	private void loadTags() {
		for (Tag tag : pin.getTags()) {

			View tagView = ((LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.view_tag_button, tagLayout, false);
			TextView title = (TextView) tagView.findViewById(tagTitle);
			title.setText(tag.getTitle());
			tagLayout.addView(tagView);

			tagView.setOnLongClickListener(view -> {
				itemMap = new HashMap<>();
				PopupMenu menu = new PopupMenu(mainActivity, view);
				menu.getMenuInflater().inflate(R.menu.remove_tag, menu.getMenu());
				menu.setOnMenuItemClickListener(this::onOptionsItemSelected);
				itemMap.put(menu.getMenu().getItem(0), new TagView(tag, view));
				menu.show();

				return true;
			});
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.actionRemoveTag) {
			TagView tagView = itemMap.get(item);
			realm.executeTransaction(r -> pin.getTags().remove(tagView.tag));
			((LinearLayout)tagView.view.getParent()).removeView(tagView.view);
		}
		return super.onOptionsItemSelected(item);
	}

	private class TagView {
		public Tag tag;
		public View view;

		public TagView(Tag tag, View view) {
			this.tag = tag;
			this.view = view;
		}

	}
}
