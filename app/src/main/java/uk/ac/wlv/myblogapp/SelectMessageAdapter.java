package uk.ac.wlv.myblogapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

// Adapter used to select multiple messages
public class SelectMessageAdapter extends RecyclerView.Adapter<SelectMessageAdapter.SelectViewHolder> {

    public interface OnSelectionChangeListener {
        void onSelectionChanged();
    }

    private Context context;
    private ArrayList<Message> messageList;
    private OnSelectionChangeListener listener;

    public SelectMessageAdapter(Context context, ArrayList<Message> messageList, OnSelectionChangeListener listener) {
        this.context = context;
        this.messageList = messageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SelectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_select_message, parent, false);
        return new SelectViewHolder(view);
    }

    // Shows each message with checkbox and image preview
    @Override
    public void onBindViewHolder(@NonNull SelectViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.selectItemTitle.setText(message.getTitle());
        holder.selectItemContent.setText(message.getContent());

        if (message.getImagePath() != null && !message.getImagePath().isEmpty()) {
            File imageFile = new File(message.getImagePath());

            if (imageFile.exists()) {
                holder.selectItemImage.setImageURI(Uri.fromFile(imageFile));
                holder.selectItemImage.setVisibility(View.VISIBLE);
            } else {
                holder.selectItemImage.setVisibility(View.GONE);
            }
        } else {
            holder.selectItemImage.setVisibility(View.GONE);
        }

        holder.messageCheckBox.setOnCheckedChangeListener(null);
        holder.messageCheckBox.setChecked(message.isSelected());

        // Updates selected status when checkbox is clicked
        holder.messageCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            message.setSelected(isChecked);
            if (listener != null) {
                listener.onSelectionChanged();
            }
        });

        // Allows full item click to select or unselect message
        holder.itemView.setOnClickListener(v -> {
            boolean newValue = !message.isSelected();
            message.setSelected(newValue);
            holder.messageCheckBox.setChecked(newValue);

            if (listener != null) {
                listener.onSelectionChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // Returns only selected messages
    public ArrayList<Message> getSelectedMessages() {
        ArrayList<Message> selectedMessages = new ArrayList<>();

        for (Message message : messageList) {
            if (message.isSelected()) {
                selectedMessages.add(message);
            }
        }

        return selectedMessages;
    }

    // Selects all messages
    public void selectAllMessages() {
        for (Message message : messageList) {
            message.setSelected(true);
        }

        notifyDataSetChanged();

        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    // Clears all selected messages
    public void clearSelection() {
        for (Message message : messageList) {
            message.setSelected(false);
        }

        notifyDataSetChanged();

        if (listener != null) {
            listener.onSelectionChanged();
        }
    }

    public static class SelectViewHolder extends RecyclerView.ViewHolder {

        CheckBox messageCheckBox;
        ImageView selectItemImage;
        TextView selectItemTitle, selectItemContent;

        public SelectViewHolder(@NonNull View itemView) {
            super(itemView);

            messageCheckBox = itemView.findViewById(R.id.messageCheckBox);
            selectItemImage = itemView.findViewById(R.id.selectItemImage);
            selectItemTitle = itemView.findViewById(R.id.selectItemTitle);
            selectItemContent = itemView.findViewById(R.id.selectItemContent);
        }
    }
}