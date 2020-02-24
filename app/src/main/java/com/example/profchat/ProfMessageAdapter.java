package com.example.profchat;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import java.util.List;

public class ProfMessageAdapter extends ArrayAdapter<ProfMessage> {

    private List<ProfMessage> messages;
    private Activity activity;

    public ProfMessageAdapter(@NonNull Activity context, int resource, List<ProfMessage> messages) {
        super(context, resource, messages);
        this.messages = messages;
        this.activity = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        ProfMessage profMessage = getItem(position);
        int layoutresource = 0;
        int viewType = getItemViewType(position);

        if (viewType == 0) {
            layoutresource = R.layout.my_message_item;
        } else {
            layoutresource = R.layout.your_message_item;
        }

        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(layoutresource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        boolean isText = profMessage.getImageUrl() == null;

        if (isText) {
            viewHolder.messageTextView.setVisibility(View.VISIBLE);
            viewHolder.photoImageView.setVisibility(View.GONE);
            viewHolder.messageTextView.setText(profMessage.getText());
        } else {
            viewHolder.messageTextView.setVisibility(View.GONE);
            viewHolder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(viewHolder.photoImageView.getContext()).load(profMessage.getImageUrl())
                    .into(viewHolder.photoImageView);
        }

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        ProfMessage profMessage = messages.get(position);
        if (profMessage.isMine()) {
            flag = 0;
        } else {
            flag = 1;
        }
        return flag;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private class ViewHolder {
        private TextView messageTextView;
        private ImageView photoImageView;

        public ViewHolder(View view) {
            photoImageView = view.findViewById(R.id.photoImageView);
            messageTextView = view.findViewById(R.id.messageTextView);
        }
    }
}
