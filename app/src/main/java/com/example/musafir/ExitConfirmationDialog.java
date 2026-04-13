package com.example.musafir;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.Nullable;

import jp.wasabeef.blurry.Blurry;

public class ExitConfirmationDialog extends DialogFragment {


    private ViewGroup decorView;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_custom_confirm, null);
        builder.setView(view);

        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);

        btnYes.setTextSize(18);
        btnNo.setTextSize(18);

        AlertDialog dialog = builder.create();

        // --- تطبيق Blurry ---
        decorView = (ViewGroup) requireActivity().getWindow().getDecorView();
        Blurry.with(requireContext()).radius(15).sampling(2).onto(decorView);

        setCancelable(true);

        // --- أزرار ---
        btnYes.setOnClickListener(v -> {
            dismiss();
            requireActivity().finish();
        });

        btnNo.setOnClickListener(v -> dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog);
        }

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        removeBlur();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        removeBlur();
    }

    private void removeBlur() {
        if (decorView != null) {
            Blurry.delete(decorView);
            decorView = null;
        }
    }
}


