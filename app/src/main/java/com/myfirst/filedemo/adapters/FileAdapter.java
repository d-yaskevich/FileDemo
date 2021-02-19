package com.myfirst.filedemo.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myfirst.filedemo.managers.FileManager;
import com.myfirst.filedemo.models.MyFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {

    private FileManager fileManager = FileManager.instance.get();

    private final LayoutInflater inflater;
    private final ArrayList<File> files = new ArrayList<>();
    private final ArrayList<Uri> uris = new ArrayList<>();
//    private final ArrayList<MyFile> files = new ArrayList<>();
    private final OnItemClickListener listener;

    public FileAdapter(Context context, OnItemClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(File file, Uri uri);
//        void onDeleteItemClick(File file, Uri uri); // for example
    }

    public void clear() {
        files.clear();
        uris.clear();
        notifyDataSetChanged();
    }

    public void addReplaceUri(Uri uri) {
        int index = uris.indexOf(uri);
        if (index < 0) {
            index = uris.size();
            uris.add(uri);
            notifyItemInserted(files.size() + index);
        } else {
            uris.remove(index);
            uris.add(index, uri);
            notifyItemChanged(files.size() + index);
        }
    }

    public void addFiles(File[] files) {
        int index = this.files.size();
        this.files.addAll(Arrays.asList(files));
        notifyItemRangeInserted(index, files.length);
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = null;
        Uri uri = null;
        try {
            file = files.get(position);
        } catch (IndexOutOfBoundsException e) {
            uri = uris.get(position - files.size());
        }
        holder.onBind(file, uri);

        File finalFile = file;
        Uri finalUri = uri;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(finalFile, finalUri);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size() + uris.size();
    }

    class FileViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvFileName;
        private final TextView tvFilePath;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileName = itemView.findViewById(android.R.id.text1);
            tvFilePath = itemView.findViewById(android.R.id.text2);
        }

        public void onBind(File file, Uri uri) {
            String fileName = null;
            if (file != null) {
                fileName = file.getName();
            } else if (uri != null) {
                fileName = fileManager.getFileName(uri);
            }
            tvFileName.setText(fileName);

            if (file != null) {
                String filePath = file.getPath();
                tvFilePath.setText(filePath);
            }
        }
    }
}
