package com.example.legokp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.legokp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;

import java.util.List;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private RadioGroup rgSortBy;
    private ChipGroup cgPriceRange, cgAgeRange;
    private RangeSlider sliderParts;
    private Button btnApply, btnReset;

    private OnFilterAppliedListener listener;

    // Текущие фильтры
    private String sortBy = "name_asc";
    private String priceRange = null;
    private String ageRange = null;
    private int minParts = 0;
    private int maxParts = 5000;

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
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        loadSavedFilters();
    }

    private void initViews(View view) {
        rgSortBy = view.findViewById(R.id.rgSortBy);
        cgPriceRange = view.findViewById(R.id.cgPriceRange);
        cgAgeRange = view.findViewById(R.id.cgAgeRange);
        sliderParts = view.findViewById(R.id.sliderParts);
        btnApply = view.findViewById(R.id.btnApply);
        btnReset = view.findViewById(R.id.btnReset);
    }

    private void setupListeners() {
        // Sort By
        rgSortBy.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbNameAsc) {
                sortBy = "name_asc";
            } else if (checkedId == R.id.rbNameDesc) {
                sortBy = "name_desc";
            } else if (checkedId == R.id.rbPriceLow) {
                sortBy = "price_asc";
            } else if (checkedId == R.id.rbPriceHigh) {
                sortBy = "price_desc";
            } else if (checkedId == R.id.rbRating) {
                sortBy = "rating_desc";
            } else if (checkedId == R.id.rbNewest) {
                sortBy = "year_desc";
            }
        });

        // Price Range Chips
        setupChipGroup(cgPriceRange, selected -> priceRange = selected);

        // Age Range Chips
        setupChipGroup(cgAgeRange, selected -> ageRange = selected);

        // Parts Slider
        sliderParts.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            if (values.size() >= 2) {
                minParts = values.get(0).intValue();
                maxParts = values.get(1).intValue();
            }
        });

        // Apply Button
        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                FilterOptions options = new FilterOptions(
                        sortBy, priceRange, ageRange, minParts, maxParts
                );
                listener.onFilterApplied(options);
            }
            dismiss();
        });

        // Reset Button
        btnReset.setOnClickListener(v -> {
            resetFilters();
        });
    }

    private void setupChipGroup(ChipGroup chipGroup, OnChipSelectedListener listener) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        listener.onChipSelected(chip.getText().toString());
                    } else {
                        // Если отжали чип, сбрасываем фильтр
                        listener.onChipSelected(null);
                    }
                });
            }
        }
    }

    private void loadSavedFilters() {
        // Загружаем сохраненные фильтры из Bundle если есть
        if (getArguments() != null) {
            sortBy = getArguments().getString("sortBy", "name_asc");
            priceRange = getArguments().getString("priceRange");
            ageRange = getArguments().getString("ageRange");
            minParts = getArguments().getInt("minParts", 0);
            maxParts = getArguments().getInt("maxParts", 5000);

            // Применяем к UI
            applySavedFiltersToUI();
        }
    }

    private void applySavedFiltersToUI() {
        // Sort By
        if ("name_asc".equals(sortBy)) {
            rgSortBy.check(R.id.rbNameAsc);
        } else if ("name_desc".equals(sortBy)) {
            rgSortBy.check(R.id.rbNameDesc);
        } else if ("price_asc".equals(sortBy)) {
            rgSortBy.check(R.id.rbPriceLow);
        } else if ("price_desc".equals(sortBy)) {
            rgSortBy.check(R.id.rbPriceHigh);
        } else if ("rating_desc".equals(sortBy)) {
            rgSortBy.check(R.id.rbRating);
        } else if ("year_desc".equals(sortBy)) {
            rgSortBy.check(R.id.rbNewest);
        }

        // Parts Slider
        sliderParts.setValues((float) minParts, (float) maxParts);
    }

    private void resetFilters() {
        sortBy = "name_asc";
        priceRange = null;
        ageRange = null;
        minParts = 0;
        maxParts = 5000;

        rgSortBy.check(R.id.rbNameAsc);
        cgPriceRange.clearCheck();
        cgAgeRange.clearCheck();
        sliderParts.setValues(0f, 5000f);
    }

    private interface OnChipSelectedListener {
        void onChipSelected(String selected);
    }

    // Класс для хранения параметров фильтра
    public static class FilterOptions {
        public String sortBy;
        public String priceRange;
        public String ageRange;
        public int minParts;
        public int maxParts;

        public FilterOptions(String sortBy, String priceRange, String ageRange,
                             int minParts, int maxParts) {
            this.sortBy = sortBy;
            this.priceRange = priceRange;
            this.ageRange = ageRange;
            this.minParts = minParts;
            this.maxParts = maxParts;
        }
    }
}