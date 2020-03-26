package com.lawtest.ui.base;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;

import com.lawtest.R;
import com.lawtest.model.ReviewForList;

import java.util.ArrayList;

// класс, визуализирующий строки в list view. Общий для всех списков, визуализирующих отзывы
public class BaseReviewListAdapter extends ArrayAdapter<ReviewForList> {
    private FragmentActivity activity;

    // subclass содержащий ссылки на элементы интерфейса в строке. Здесь, в отличие от стандартной
    // схемы ViewHolder сам следит за изменениями своих элементров и заполняет их.
    private class ViewHolder {
        ImageView image;
        TextView name;
        TextView body;

        // в конструктор передаются ссылки, на элементы интерфейса в строке
        ViewHolder(
                ImageView image,
                TextView name,
                TextView body,
                ReviewForList review
        ) {
            this.image = image;
            this.name = name;
            this.body = body;
            // коллбак на изменения с сервера для получения данных и заполнения элементов интерфейса
            review.getReview().observe(activity, new Observer<ReviewForList>() {
                @Override
                public void onChanged(ReviewForList review) {
                    if (review.getAvatarUri() != null) ViewHolder.this.image.setImageURI(review.getAvatarUri());
                    else ViewHolder.this.image.setImageResource(R.drawable.ic_user_default);
                    ViewHolder.this.name.setText(review.fName + " " + review.surName);
                    ViewHolder.this.body.setText(review.body);
                }
            });
        }
    }

    public BaseReviewListAdapter(FragmentActivity activity) {
        super(activity, R.layout.review_list_row);

        this.activity = activity;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            // получение view строки и инициализация ViewHolder
            rowView = inflater.inflate(R.layout.review_list_row, null, true);
            holder = new ViewHolder(
                    (ImageView) rowView.findViewById(R.id.review_row_ava),
                    (TextView) rowView.findViewById(R.id.reviewName),
                    (TextView) rowView.findViewById(R.id.reviewBody),
                    getItem(position)
            );
            rowView.setTag(holder);
        }

        return rowView;
    }

    // обновление списка отзывов по переданному списку
    public void updateReviews(ArrayList<ReviewForList> reviews) {
        clear();
        addAll(reviews);
        notifyDataSetChanged();
    }
}
