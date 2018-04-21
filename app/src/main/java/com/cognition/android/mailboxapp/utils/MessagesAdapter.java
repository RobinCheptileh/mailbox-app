package com.cognition.android.mailboxapp.utils;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.cognition.android.mailboxapp.R;
import com.cognition.android.mailboxapp.models.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> implements Filterable {

    private Context mContext;
    private Utils mUtils;
    private List<Message> messageList;
    private List<Message> messageListFiltered;

    private ViewGroup parent;

    class MessageViewHolder extends RecyclerView.ViewHolder {

        LinearLayoutCompat lytItemParent;
        ConstraintLayout lytFromPreviewParent;
        AppCompatTextView txtFromPreview, txtFrom, txtDate, txtSubject, txtSnippet;

        public MessageViewHolder(View itemView) {
            super(itemView);

            lytItemParent = itemView.findViewById(R.id.lytItemParent);
            lytFromPreviewParent = itemView.findViewById(R.id.lytFromPreviewParent);
            txtFromPreview = itemView.findViewById(R.id.txtFromPreview);
            txtFrom = itemView.findViewById(R.id.txtFrom);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtSubject = itemView.findViewById(R.id.txtSubject);
            txtSnippet = itemView.findViewById(R.id.txtSnippet);
        }
    }

    public MessagesAdapter(Context context, List<Message> messageList) {
        this.mContext = context;
        this.mUtils = new Utils(context);
        this.messageList = messageList;
        this.messageListFiltered = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parent = parent;
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);

        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        final Message message = this.messageListFiltered.get(position);

        holder.lytItemParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUtils.showSnackbar(MessagesAdapter.this.parent, message.getFrom());
            }
        });

        android.graphics.drawable.GradientDrawable gradientDrawable = (android.graphics.drawable.GradientDrawable) holder.lytFromPreviewParent.getBackground();
        gradientDrawable.setColor(message.getColor());

        holder.txtFromPreview.setText(message.getFrom().substring(0, 1).toUpperCase(Locale.ENGLISH));
        holder.txtFrom.setText(message.getFrom());
        holder.txtDate.setText(mUtils.timestampToDate(message.getTimestamp()));
        holder.txtSubject.setText(message.getSubject());
        holder.txtSnippet.setText(message.getSnippet());
    }

    @Override
    public int getItemCount() {
        return this.messageListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String searchText = constraint.toString().trim().toLowerCase();

                if (searchText.isEmpty()) {
                    MessagesAdapter.this.messageListFiltered = MessagesAdapter.this.messageList;
                } else {
                    List<Message> newMessageList = new ArrayList<>();

                    for (Message message : MessagesAdapter.this.messageList) {
                        if (message.getFrom().toLowerCase().contains(searchText)
                                || message.getSubject().toLowerCase().contains(searchText)
                                || message.getSnippet().toLowerCase().contains(searchText)
                                || mUtils.timestampToDate(message.getTimestamp()).toLowerCase().contains(searchText)
                                )
                            newMessageList.add(message);
                    }

                    MessagesAdapter.this.messageListFiltered = newMessageList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = MessagesAdapter.this.messageListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                MessagesAdapter.this.messageListFiltered = (ArrayList<Message>) results.values;
                notifyDataSetChanged();
            }
        };
    }

}
