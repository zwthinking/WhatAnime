package pw.janyo.whatanime.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pw.janyo.whatanime.R;
import pw.janyo.whatanime.classes.Dock;
import pw.janyo.whatanime.util.TextViewUtils;

public class AnimationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private static final int IMAGE_TYPE = 1;
	private static final int ANIME_TYPE = 2;
	private Context context;
	private List<Dock> list;
	private String imgPath = null;
	private HeaderClickListener headerClickListener;
	private RequestOptions options = new RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE);

	public AnimationAdapter(Context context, List<Dock> list) {
		this.context = context;
		this.list = list;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
		notifyDataSetChanged();
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setHeaderClickListener(HeaderClickListener headerClickListener) {
		this.headerClickListener = headerClickListener;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0)
			return IMAGE_TYPE;
		return ANIME_TYPE;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		switch (viewType) {
			case IMAGE_TYPE:
				View imageView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
				return new ImageViewHolder(imageView);
			case ANIME_TYPE:
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_animation, parent, false);
				return new ViewHolder(view);
			default:
				return null;
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof ViewHolder) {
			final ViewHolder viewHolder = (ViewHolder) holder;
			Dock dock = list.get(position - 1);
			StringBuilder stringBuilder = new StringBuilder();
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.CHINA);
			stringBuilder.append("标题：").append(dock.title).append('\n');
			stringBuilder.append("中文标题：").append(dock.title_chinese).append('\n');
			calendar.setTimeInMillis((long) dock.at * 1000);
			stringBuilder.append("时间：").append(dateFormat.format(calendar.getTime())).append('\n');
			stringBuilder.append("准确度：").append(dock.similarity * 100).append('%').append('\n');
			stringBuilder.append("episode：").append(dock.episode).append('\n');
			stringBuilder.append("anilist_id：").append(dock.anilist_id).append('\n');
			stringBuilder.append("mal_id：").append(dock.mal_id).append('\n');
			stringBuilder.append("日语标题：").append(dock.title_native).append('\n');
			stringBuilder.append("英文标题：").append(dock.title_english).append('\n');
			stringBuilder.append("罗马字：").append(dock.title_romaji).append('\n');
			viewHolder.textView.setText(stringBuilder.toString());
			try {
				String requestUrl = "https://whatanime.ga/thumbnail.php?season=" + dock.season + "&anime=" + URLEncoder.encode(dock.anime, "UTF-8") + "&file=" + URLEncoder.encode(dock.filename, "UTF-8") + "&t=" + dock.at + "&token=" + dock.tokenthumb;
				Glide.with(context).load(requestUrl).into(viewHolder.imageView);
			} catch (Exception e) {
				e.printStackTrace();
			}
			TextViewUtils.setMaxLinesWithAnimation(viewHolder.textView, 4);
			viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					TextViewUtils.setMaxLinesWithAnimation(viewHolder.textView, viewHolder.textView.getMaxLines() == Integer.MAX_VALUE ? 4 : Integer.MAX_VALUE);
				}
			});
//			viewHolder.text_name.setText(dock.title);
//			viewHolder.text_chinese_name.setText(dock.title_chinese);
//			StringBuilder synonyms_chinese = new StringBuilder();
//			if (dock.synonyms_chinese.length != 0)
//				for (String temp : dock.synonyms_chinese)
//					synonyms_chinese.append(temp).append("\n");
//			else
//				synonyms_chinese.append("无");
//			viewHolder.text_chinese_name_synonyms.setText(synonyms_chinese);
//			viewHolder.text_english_name.setText(dock.title_english);
//			StringBuilder synonyms = new StringBuilder();
//			if (dock.synonyms.length != 0)
//				for (String temp : dock.synonyms)
//					synonyms.append(temp).append("\n");
//			else
//				synonyms.append("无");
//			viewHolder.text_english_name_synonyms.setText(synonyms);
//			viewHolder.text_romaji_name.setText(dock.title_romaji);
//			viewHolder.text_season.setText(dock.season);
//			viewHolder.text_episode.setText(dock.episode);
//			viewHolder.text_aniListId.setText(dock.anilist_id);
//			String similarity = String.valueOf(dock.similarity * 100) + '%';
//			viewHolder.text_similarity.setText(similarity);
//			Calendar calendar = Calendar.getInstance();
//			SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss", Locale.CHINA);
//			calendar.setTimeInMillis((long) dock.from * 1000);
//			String from = dateFormat.format(calendar.getTime());
//			calendar.setTimeInMillis(((long) dock.to * 1000));
//			String to = dateFormat.format(calendar.getTime());
//			calendar.setTimeInMillis(((long) dock.at * 1000));
//			String at = dateFormat.format(calendar.getTime());
//			viewHolder.text_time.setText(context.getString(R.string.time, from, to, at));
		} else if (holder instanceof ImageViewHolder) {
			ImageViewHolder viewHolder = (ImageViewHolder) holder;
			if (imgPath != null)
				Glide.with(context).load(imgPath).apply(options).into(viewHolder.imageView);
			if (headerClickListener != null)
				viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						headerClickListener.onClick();
					}
				});
		}
	}

	@Override
	public int getItemCount() {
		return list.size() + 1;
	}

	class ImageViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;

		ImageViewHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
		}
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		ImageView imageView;
		TextView textView;

		ViewHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.imageView);
			textView = itemView.findViewById(R.id.textView);
		}
	}

	public interface HeaderClickListener {
		public void onClick();
	}
}
