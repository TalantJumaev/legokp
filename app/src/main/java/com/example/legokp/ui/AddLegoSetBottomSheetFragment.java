package com.example.legokp.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.legokp.R;
import com.example.legokp.models.LegoSet;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.UUID;

public class AddLegoSetBottomSheetFragment extends BottomSheetDialogFragment {

    private TextInputEditText etSetName, etSetYear, etSetTheme, etSetNumParts, etSetImageUrl, etSetPrice, etSetRating, etSetAgeRange, etSetDescription;
    // ✨ ИСПРАВЛЕНО: Возвращена ссылка на switchIsExclusive
    private SwitchMaterial switchIsExclusive;
    private Button btnCancel, btnSave;

    private AddSetListener listener;

    public interface AddSetListener {
        void onSetAdded(LegoSet legoSet);
    }

    public static AddLegoSetBottomSheetFragment newInstance(AddSetListener listener) {
        AddLegoSetBottomSheetFragment fragment = new AddLegoSetBottomSheetFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_lego_set, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        etSetName = view.findViewById(R.id.etSetName);
        etSetYear = view.findViewById(R.id.etSetYear);
        etSetTheme = view.findViewById(R.id.etSetTheme);
        etSetNumParts = view.findViewById(R.id.etSetNumParts);
        etSetImageUrl = view.findViewById(R.id.etSetImageUrl);
        etSetPrice = view.findViewById(R.id.etSetPrice);
        etSetRating = view.findViewById(R.id.etSetRating);
        etSetAgeRange = view.findViewById(R.id.etSetAgeRange);
        etSetDescription = view.findViewById(R.id.etSetDescription);
        // ✨ ИСПРАВЛЕНО: Инициализация switchIsExclusive
        switchIsExclusive = view.findViewById(R.id.switchIsExclusive);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveLegoSet());
    }

    private void saveLegoSet() {
        String name = etSetName.getText().toString().trim();
        String yearStr = etSetYear.getText().toString().trim();
        String theme = etSetTheme.getText().toString().trim();
        String numPartsStr = etSetNumParts.getText().toString().trim();
        String imageUrl = etSetImageUrl.getText().toString().trim();
        String priceStr = etSetPrice.getText().toString().trim();
        String ratingStr = etSetRating.getText().toString().trim();
        String ageRange = etSetAgeRange.getText().toString().trim();
        String description = etSetDescription.getText().toString().trim();

        if (name.isEmpty() || yearStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(getContext(), "Name, Year, and Price are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            double price = Double.parseDouble(priceStr);
            double rating = ratingStr.isEmpty() ? 0.0 : Double.parseDouble(ratingStr);
            int numParts = numPartsStr.isEmpty() ? 0 : Integer.parseInt(numPartsStr);
            // ✨ ИСПРАВЛЕНО: Считывание значения для isExclusive
            boolean isExclusive = switchIsExclusive.isChecked();

            LegoSet newSet = new LegoSet();
            newSet.setSetNum("CUSTOM-" + UUID.randomUUID().toString().substring(0, 8));
            newSet.setName(name);
            newSet.setYear(year);
            newSet.setTheme(theme.isEmpty() ? "Custom" : theme);
            newSet.setSetImgUrl(imageUrl);
            newSet.setPrice(price);
            newSet.setRating(rating);
            newSet.setAgeRange(ageRange.isEmpty() ? "N/A" : ageRange);
            newSet.setDescription(description);
            newSet.setNumParts(numParts);
            
            // ✨ ИСПРАВЛЕНО: Установка значения isExclusive
            newSet.setExclusive(isExclusive);
            newSet.setInStock(true); // Значение по умолчанию

            if (listener != null) {
                listener.onSetAdded(newSet);
            }
            dismiss();

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers for Year, Price, Parts and Rating.", Toast.LENGTH_SHORT).show();
        }
    }
}