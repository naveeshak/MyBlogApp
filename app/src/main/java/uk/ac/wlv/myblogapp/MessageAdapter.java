package uk.ac.wlv.myblogapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

// Adapter used to show saved messages in the main message list
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private ArrayList<Message> messageList;

    public MessageAdapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    // Binds message title, content, date and image to each list item
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.itemTitle.setText(message.getTitle());
        holder.itemContent.setText(message.getContent());
        holder.itemDate.setText(message.getDateTime());

        if (message.getImagePath() != null && !message.getImagePath().isEmpty()) {
            File imageFile = new File(message.getImagePath());

            if (imageFile.exists()) {
                holder.itemImage.setImageURI(Uri.fromFile(imageFile));
                holder.itemImage.setVisibility(View.VISIBLE);
            } else {
                holder.itemImage.setVisibility(View.GONE);
            }
        } else {
            holder.itemImage.setVisibility(View.GONE);
        }

        // Opens the selected message in view screen
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewMessageActivity.class);
            intent.putExtra("MESSAGE_ID", message.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage;
        TextView itemTitle, itemContent, itemDate;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            itemImage = itemView.findViewById(R.id.itemImage);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemContent = itemView.findViewById(R.id.itemContent);
            itemDate = itemView.findViewById(R.id.itemDate);
        }
    }
}