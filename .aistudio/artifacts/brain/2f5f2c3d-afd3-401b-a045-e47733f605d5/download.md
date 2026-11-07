# 📥 Download Global Currency Converter APK (v1.2)

Your updated, signed Android APK is ready for local installation and testing:

* **File Name:** `GlobalCurrencyConverter.apk`
* **Size:** `25.6 MB` (26,885,685 bytes)
* **Package ID:** `com.globalcurrency.converter`
* **Version:** 1.2 (API 24 - 36)
* **SHA-256:** `6ff045f28414973fd109f69b498113c23346a49b54b52c048038a91f4f84a1f2`

---

### **Direct Download Links**

* [⬇️ **Download GlobalCurrencyConverter.apk (Artifact)**](GlobalCurrencyConverter.apk)
* [⬇️ **Download from Project Root**](/GlobalCurrencyConverter.apk)
* [⬇️ **Download from Build Output**](app/build/outputs/apk/debug/GlobalCurrencyConverter.apk)

---

### **What's New in Version 1.2**

1. **Fully Visible Amounts (Zero Clipping):**
   - Fixed the Nigerian Naira (NGN) display issue where large converted amounts were vertically clipped.
   - Removed fixed-height constraints; currency cards are now dynamically responsive to their content (`wrapContentHeight`).
   - Moved the amount to its own dedicated full-width row underneath the header row, giving it 100% of the card's width.

2. **Auto-Resizing Font Scaling (`AutoResizingAmountText`):**
   - Automatically adapts font size based on digit count and available screen width:
     - `100.00` (<= 7 chars): Bold `32.sp`
     - `134,810.63` (8–11 chars): Bold `26.sp`
     - `1,234,567.89` (12–15 chars): Bold `22.sp`
     - `123,456,789.1234` (16–19 chars): Bold `18.sp`
     - Very large numbers (> 19 chars): Down to `14.sp` with continuous overflow detection.
   - Strictly keeps amounts on a single line (`softWrap = false`, `maxLines = 1`) aligned neatly to the right.
   - **No truncation, no ellipses (`...`), no hidden decimal digits, and no cropping.**

3. **Responsive Spacing & Adaptive Layout:**
   - Generous padding (`18.dp` horizontal, `14.dp` vertical) inside cards.
   - Conversion container wrapped in a smooth `verticalScroll` state so cards never get squashed even on compact screens or with large system font/display scaling.
   - Compact calculator keypad (`50.dp` buttons) to ensure cards have maximum vertical real estate.

4. **1 USD = 1,348.11 NGN Baseline:**
   - Default baseline rate for Nigerian Naira set to `1,348.11` so converting `100 USD` immediately results in `134,810.63 NGN` on fresh launch.

---

### **Installation Instructions for Android Phones**

1. Download **`GlobalCurrencyConverter.apk`** to your phone or computer.
2. If downloaded on a computer, transfer via USB or run `adb install -r GlobalCurrencyConverter.apk`.
3. On phone, open the **Files / Downloads** app and tap **`GlobalCurrencyConverter.apk`**.
4. Allow installation from unknown sources if prompted.
5. Tap **Install**, then **Open**.
