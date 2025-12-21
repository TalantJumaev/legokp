package com.example.legokp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.legokp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    public static class FilterOptions {
        public String sortBy = "name_asc";
        public String theme;
        public int minYear = 1970;
        public int maxYear = 2024;
        public int minParts = 0;
        public int maxParts = 10000;
    }

    private OnFilterAppliedListener listener;

    public interface OnFilterAppliedListener {
        void onFilterApplied(FilterOptions options);
    }

    public static FilterBottomSheetFragment newInstance(OnFilterAppliedListener listener) {
        FilterBottomSheetFragment fragment = new FilterBottomSheetFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etThemeFilter = view.findViewById(R.id.etThemeFilter);
        RangeSlider rsYear = view.findViewById(R.id.rsYear);
        TextView tvYearValue = view.findViewById(R.id.tvYearValue);
        RangeSlider rsParts = view.findViewById(R.id.rsParts);
        TextView tvPartsValue = view.findViewById(R.id.tvPartsValue);
        Button btnApply = view.findViewById(R.id.btnApply);
        // ✨ НОВОЕ: Инициализация RadioGroup для сортировки
        RadioGroup rgSort = view.findViewById(R.id.rgSort);

        rsYear.addOnChangeListener((slider, value, fromUser) -> {
            int min = slider.getValues().get(0).intValue();
            int max = slider.getValues().get(1).intValue();
            tvYearValue.setText(min + " - " + max);
        });

        rsParts.addOnChangeListener((slider, value, fromUser) -> {
            int min = slider.getValues().get(0).intValue();
            int max = slider.getValues().get(1).intValue();
            tvPartsValue.setText(min + " - " + max);
        });
        
        tvYearValue.setText(String.format("%d - %d", rsYear.getValues().get(0).intValue(), rsYear.getValues().get(1).intValue()));
        tvPartsValue.setText(String.format("%d - %d", rsParts.getValues().get(0).intValue(), rsParts.getValues().get(1).intValue()));

        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                FilterOptions options = new FilterOptions();
                // ✨ НОВОЕ: Получаем опцию сортировки
                options.sortBy = getSortBy(rgSort.getCheckedRadioButtonId());
                options.theme = etThemeFilter.getText().toString();
                options.minYear = rsYear.getValues().get(0).intValue();
                options.maxYear = rsYear.getValues().get(1).intValue();
                options.minParts = rsParts.getValues().get(0).intValue();
                options.maxParts = rsParts.getValues().get(1).intValue();
                listener.onFilterApplied(options);
            }
            dismiss();
        });
    }

    // ✨ НОВОЕ: Метод для определения опции сортировки
    private String getSortBy(int checkedId) {
        if (checkedId == R.id.rbSortNameAsc) return "name_asc";
        if (checkedId == R.id.rbSortNameDesc) return "name_desc";
        if (checkedId == R.id.rbSortPriceAsc) return "price_asc";
        if (checkedId == R.id.rbSortPriceDesc) return "price_desc";
        return "name_asc"; // По умолчанию
    }
}
