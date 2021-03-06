package ch.ralena.personalpins.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ch.ralena.personalpins.R;
import ch.ralena.personalpins.objects.Tag;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class TagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	List<Tag> tags;

	private PublishSubject<Tag> onClickTag = PublishSubject.create();

	public TagsAdapter(List<Tag> tags) {
		this.tags = tags;
	}

	public Observable<Tag> asObservable() {
		return onClickTag;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		((ViewHolder)holder).bindView(tags.get(position));
	}

	@Override
	public int getItemCount() {
		return tags.size();
	}

	private class ViewHolder extends RecyclerView.ViewHolder {
		private TextView tagName;
		public ViewHolder(View itemView) {
			super(itemView);
			tagName = (TextView) itemView.findViewById(R.id.tagName);
		}

		public void bindView(Tag tag) {
			tagName.setText(tag.getTitle());
			itemView.setOnClickListener(v -> onClickTag.onNext(tag));
		}
	}
}
