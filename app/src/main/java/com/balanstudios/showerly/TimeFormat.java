package com.balanstudios.showerly;


import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

//formats time inputs while user is typing using Text Watcher and textToValue()
 class TimeFormat {
    static Context context;
    EditText et;
    String raw;
    private TextWatcher timeFormatter = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().length() > 0) {
                raw += charSequence.toString();
                if (raw.length() > charSequence.length()) {
                    raw = charSequence.toString().replaceAll("m", "");
                    raw = raw.replaceAll(" ", "");
                    raw = raw.replaceAll("s", "");
                    et.removeTextChangedListener(this);
                    et.setText(textToValue(raw));
                    et.addTextChangedListener(this);
                    et.setSelection(textToValue(raw).length() - 1);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public TimeFormat(EditText et, Context context) {
        this.et = et;
        this.context = context;
    }

    public TextWatcher getTimeFormatter() {
        return timeFormatter;
    }

    //user can input 010 to get 0m 10s or 2030 to get 20m 30s, also accounts for m and s being in the text. Returns seconds
    public static int textToValue(EditText et) {
        String raw = et.getText().toString().trim();
        int min, sec, index = 2;

        try { //prone to crashing if user deletes only part of the text but not all and then enters
            if (raw.contains("m") || raw.contains("s")) {
                index = 5;


                min = Integer.parseInt(
                        raw.substring(0, raw.length() - index));
                sec = Integer.parseInt(
                        raw.substring(raw.length() - index + 2, raw.length() - 1));
                if (sec >= 60) {
                    min += sec / 60;
                    sec = sec % 60;
                }


            } else {
                if (raw.length() < 3) {
                    min = 0;
                    sec = Integer.parseInt(raw);
                } else {
                    min = Integer.parseInt(
                            raw.substring(0, raw.length() - index));
                    sec = Integer.parseInt(
                            raw.substring(raw.length() - index));
                }


                if (sec >= 60) {
                    min += sec / 60;
                    sec = sec % 60;
                }
            }

            et.setText(String.format(Locale.getDefault(), "%dm %02ds", min, sec));
            return (min * 60 + sec);

        } catch (NumberFormatException e) {
            Toast.makeText(context, "Input proper format", Toast.LENGTH_SHORT).show();
        } catch (StringIndexOutOfBoundsException e) {
            Toast.makeText(context, "Input proper format", Toast.LENGTH_SHORT).show();
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(context, "Input proper format", Toast.LENGTH_SHORT).show();
        }
        return 0;
    }

    public String textToValue(String raw) {
        int min, sec, index = 2;

        try { //prone to crashing if user deletes only part of the text but not all and then enters
            if (raw.contains("m") || raw.contains("s")) {
                index = 5;


                min = Integer.parseInt(
                        raw.substring(0, raw.length() - index));
                sec = Integer.parseInt(
                        raw.substring(raw.length() - index + 2, raw.length() - 1));
                if (sec >= 60) {
                    min += sec / 60;
                    sec = sec % 60;
                }


            } else {
                if (raw.length() < 3) {
                    min = 0;
                    sec = Integer.parseInt(raw);
                } else {
                    min = Integer.parseInt(
                            raw.substring(0, raw.length() - index));
                    sec = Integer.parseInt(
                            raw.substring(raw.length() - index));
                }


//                if (sec >= 60) {
//                    min += sec / 60;
//                    sec = sec % 60;
//                }
            }

            return String.format(Locale.getDefault(), "%dm %02ds", min, sec);

        } catch (NumberFormatException e) {
            Toast.makeText(context, "Input proper format", Toast.LENGTH_SHORT).show();
        } catch (StringIndexOutOfBoundsException e) {
            Toast.makeText(context, "Input proper format", Toast.LENGTH_SHORT).show();
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(context, "Input proper format", Toast.LENGTH_SHORT).show();
        }
        return "";
    }

    public static String valueToString(int sec){
        String format = "";

        int min = sec / 60;
        sec %= 60;
        format = String.format(Locale.getDefault(), "%dm %02ds", min, sec);

        return format;
    }

    public static String valueToString(long sec){
        String format = "";

        long min = sec / 60;
        sec %= 60;
        format = String.format(Locale.getDefault(), "%dm %02ds", min, sec);

        return format;
    }
}

    