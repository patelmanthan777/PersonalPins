package ch.ralena.personalpins.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import ch.ralena.personalpins.R;
import ch.ralena.personalpins.objects.Pin;
import ch.ralena.personalpins.objects.Tag;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class PinsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final String TAG = PinsAdapter.class.getSimpleName();
	private static final int TYPE_PIN = 0;
	private static final int TYPE_NEW = 1;


	private final PublishSubject<PinView> onClickSubject = PublishSubject.create();
	private final PublishSubject<PinView> onLongClickSubject = PublishSubject.create();
	private final PublishSubject<View> onNewClickSubject = PublishSubject.create();


	List<Pin> pins;
	private boolean hasNewButton;
	private String newButtonText;

	public PinsAdapter(List<Pin> pins, boolean hasNewButton, String newButtonText) {
		this.pins = pins;
		this.hasNewButton = hasNewButton;
		this.newButtonText = newButtonText;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_NEW) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new, parent, false);
			return new ViewHolderNew(view);
		} else {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pin, parent, false);
			return new ViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (position < pins.size())
			((ViewHolder) holder).bindView(pins.get(position));
	}

	@Override
	public int getItemCount() {
		return hasNewButton ? pins.size() + 1 : pins.size();
	}

	@Override
	public int getItemViewType(int position) {
		return position == pins.size() ? TYPE_NEW : TYPE_PIN;
	}

	private class ViewHolder extends RecyclerView.ViewHolder {
		ImageView thumbnailImage;
		RelativeLayout videoContainer;
		VideoView thumbnailVideo;
		TextView title;
		TextView tags;

		public ViewHolder(View itemView) {
			super(itemView);
			thumbnailImage = (ImageView) itemView.findViewById(R.id.thumbnailImage);
			videoContainer = (RelativeLayout) itemView.findViewById(R.id.videoContainer);
			thumbnailVideo = (VideoView) itemView.findViewById(R.id.thumbnailVideo);
			title = (TextView) itemView.findViewById(R.id.pinTitle);
			tags = (TextView) itemView.findViewById(R.id.tags);
		}

		public void bindView(Pin pin) {
			itemView.setOnClickListener(v -> onClickSubject.onNext(new PinView(pin, thumbnailImage)));
			itemView.setOnLongClickListener(v -> {
				onLongClickSubject.onNext(new PinView(pin, thumbnailImage));
				return true;
			});

			if (pin.getFilepath() != null) {
				if (pin.getType().equals(Pin.TYPE_PICTURE)) {
					thumbnailImage.setVisibility(View.VISIBLE);
					videoContainer.setVisibility(View.GONE);
					Uri imageUri = Uri.fromFile(new File(pin.getFilepath()));
					Picasso.with(thumbnailImage.getContext())
							.load(imageUri)
							.fit()
							.centerCrop()
							.into(thumbnailImage);
				} else if (pin.getType().equals(Pin.TYPE_VIDEO)) {
					thumbnailImage.setVisibility(View.GONE);
					videoContainer.setVisibility(View.VISIBLE);
					thumbnailVideo.setVideoURI(Uri.parse(pin.getFilepath()));
					thumbnailVideo.seekTo(1);
				}
			}

			title.setText(pin.getTitle());

			StringBuilder stringBuilder = new StringBuilder();
			int position = 0;
			for (Tag tag : pin.getTags()) {
				stringBuilder.append(tag.getTitle());
				if (++position < pin.getTags().size()) {
					stringBuilder.append(", ");
				}
			}
			tags.setText(stringBuilder.toString());
		}
	}

	private class ViewHolderNew extends RecyclerView.ViewHolder {
		public ViewHolderNew(View itemView) {
			super(itemView);
			itemView.setOnClickListener(v -> onNewClickSubject.onNext(itemView));
			((TextView) itemView.findViewById(R.id.textView)).setText(newButtonText);
		}
	}

	public Observable<PinView> asPinObservable() {
		return onClickSubject;
	}

	public Observable<PinView> asPinLongClickObservable() {
		return onLongClickSubject;
	}

	public Observable<View> asNewObservable() {
		return onNewClickSubject;
	}

	public class PinView {
		private Pin pin;
		private View view;

		public PinView(Pin pin, View view) {
			this.pin = pin;
			this.view = view;
		}

		public Pin getPin() {
			return pin;
		}

		public View getView() {
			return view;
		}
	}

}
