package com.londonappbrewery.climapm;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.londonappbrewery.climapm.Utils.Product;
import com.londonappbrewery.climapm.home.WeatherController;

import java.util.ArrayList;
import java.util.List;

public class RecyclerBookmark extends RecyclerView.Adapter<RecyclerBookmark.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView imageName;
        TextView timeago;
        TextView section;
        ImageView cornerbookmark;
        LinearLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            imageName = itemView.findViewById(R.id.image_name);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            timeago = itemView.findViewById(R.id.timeago);
            section = itemView.findViewById(R.id.section);
            cornerbookmark=itemView.findViewById(R.id.cornerbookmark);
        }
    }


    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> mImageNames = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mSectionNames = new ArrayList<>();
    private ArrayList<String> mPublication = new ArrayList<>();
    private ArrayList<Product> mFinalList;
    private ArrayList<String> mIds = new ArrayList<>();
    static final String ImageName = "ImageName";
    static final String Image = "Image";
    static final String sectionNames = "sectionNames";
    static final String publication = "publication";

    private boolean clicked;
    private Context mContext;
    Dialog myDialog;

    public RecyclerBookmark(Context context, ArrayList<String> imageNames, ArrayList<String> images, ArrayList<String> sectionNames, ArrayList<String> publication, ArrayList<String> ids) {
        mImageNames = imageNames;
        mImages = images;
        mContext = context;
        mSectionNames = sectionNames;
        mPublication = publication;
        mIds=ids;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_bookmark, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Glide.with(mContext)
                .asBitmap()
                .load(mImages.get(position))
                .into(holder.image);

        holder.imageName.setText(mImageNames.get(position));
        holder.section.setText(mSectionNames.get(position));
        holder.timeago.setText(mPublication.get(position));
        final Product product = new Product(mImageNames.get(position),mImages.get(position),mSectionNames.get(position),mPublication.get(position),mIds.get(position));

        if (checkFavoriteItem(product)) {
            holder.cornerbookmark.setImageResource(R.drawable.baseline_bookmark_black_18dp);
        } else {
            holder.cornerbookmark.setImageResource(R.drawable.baseline_bookmark_border_black_18dp);
        }

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(mContext, mImageNames.get(position), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, GalleryActivity.class);
                intent.putExtra("id", mIds.get(position));
                intent.putExtra("image", mImages.get(position));
                mContext.startActivity(intent);
            }
        });

        holder.cornerbookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! checkFavoriteItem(product)) {
                    WeatherController.sharedPreference.addFavorite(mContext, product);
                    holder.cornerbookmark.setImageResource(R.drawable.baseline_bookmark_black_18dp);
                    Toast.makeText(mContext, mImageNames.get(position)+"was added to bookmarks", Toast.LENGTH_SHORT).show();
                } else {
                    WeatherController.sharedPreference.removeFavorite(mContext, product);
                    holder.cornerbookmark.setImageResource(R.drawable.baseline_bookmark_border_black_18dp);
                    Toast.makeText(mContext, mImageNames.get(position)+"was removed from favourites", Toast.LENGTH_SHORT).show();
                    mImageNames.remove(position);
                    mImages.remove(position);
                    mSectionNames.remove(position);
                    mPublication.remove(position);
                    Log.d("images", String.valueOf(mImageNames));
                    notifyItemRemoved(position+1);
                    notifyItemRangeChanged(position, getItemCount());
                }
            }
        });

        myDialog = new Dialog(mContext);


        ImageView dialogtwittermain = (ImageView) myDialog.findViewById(R.id.btntwitter);


        holder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                myDialog.setContentView(R.layout.dialog_contact);
                ImageView dialogimagemain = (ImageView) myDialog.findViewById(R.id.imagecardpost);
                final TextView dialogtextmain = (TextView) myDialog.findViewById(R.id.textcontent);
                final ImageView dialogbookmark = (ImageView) myDialog.findViewById(R.id.btnbookmark);


                if (checkFavoriteItem(product)) {
                    dialogbookmark.setImageResource(R.drawable.baseline_bookmark_black_18dp);
                } else {
                    dialogbookmark.setImageResource(R.drawable.baseline_bookmark_border_black_18dp);
                }
                Drawable bmp = holder.image.getDrawable();
                dialogimagemain.setImageDrawable(bmp);
                dialogtextmain.setText(mImageNames.get(position));
                myDialog.show();



                dialogbookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!checkFavoriteItem(product)) {
                            Toast.makeText(mContext,mImageNames.get(position)+"was added to bookmarks", Toast.LENGTH_SHORT).show();
                            WeatherController.sharedPreference.addFavorite(mContext, product);
                            dialogbookmark.setImageResource(R.drawable.baseline_bookmark_black_18dp);
                            holder.cornerbookmark.setImageResource(R.drawable.baseline_bookmark_black_18dp);

                        } else {
                            Toast.makeText(mContext, mImageNames.get(position)+"was removed from favourites", Toast.LENGTH_SHORT).show();
                            WeatherController.sharedPreference.removeFavorite(mContext, product);
                            dialogbookmark.setImageResource(R.drawable.baseline_bookmark_border_black_18dp);
                            mImageNames.remove(position);
                            mImages.remove(position);
                            mSectionNames.remove(position);
                            mPublication.remove(position);
                            Log.d("images", String.valueOf(mImageNames));
                            notifyItemRemoved(position+1);
                            notifyItemRangeChanged(position, getItemCount());

                        }
                    }
                });

                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mImageNames.size();
    }

    public boolean checkFavoriteItem(Product checkProduct) {
        boolean check = false;
        List<Product> favorites = WeatherController.sharedPreference.getFavorites(mContext);
        if (favorites != null) {
            for (Product product : favorites) {

                if (product.equals(checkProduct)) {
                    check = true;

                    break;
                }
            }
        }
        return check;
    }

}