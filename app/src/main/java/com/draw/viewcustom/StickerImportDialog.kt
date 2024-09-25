package com.draw.viewcustom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.draw.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StickerImportDialog (): BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dialogView =
            inflater.inflate(R.layout.bottom_sheet_sticker_import, container, false)
        return dialogView
    }

}