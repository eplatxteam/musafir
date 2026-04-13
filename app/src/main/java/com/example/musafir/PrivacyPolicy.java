package com.example.musafir;

import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class PrivacyPolicy extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        Button agreeButton = findViewById(R.id.agreeButton);
        TextView privacyText = findViewById(R.id.privacyText);
        TextView privacy = findViewById(R.id.privacy);
        ScrollView privacyScroll = findViewById(R.id.privacyScroll);

        String policyText =
                "\nسياسة الخصوصية\n\n" +
                        "مرحبًا بك في تطبيق زاد مسافر. باستخدامك لهذا التطبيق، فإنك توافق على الالتزام بالشروط والأحكام التالية. في حال عدم موافقتك على أي بند، يرجى عدم استخدام التطبيق.\n\n" +

                        "1. التعريفات\n" +
                        "التطبيق: تطبيق زاد مسافر وجميع الخدمات المرتبطة به.\n" +
                        "المستخدم: أي شخص يقوم بتحميل أو استخدام التطبيق.\n" +
                        "نحن / الشركة: الجهة المالكة والمشغلة للتطبيق.\n\n" +

                        "2. نطاق الخدمة\n" +
                        "• البحث عن الرحلات والفنادق\n" +
                        "• حجز تذاكر الطيران أو الإقامة\n" +
                        "• عرض العروض السياحية\n" +
                        "• تقديم معلومات السفر والوجهات\n\n" +

                        "3. شروط الاستخدام\n" +
                        "• يجب أن يكون عمر المستخدم 16 عامًا أو أكثر أو لديه موافقة قانونية.\n" +
                        "• الالتزام بتقديم معلومات صحيحة ودقيقة.\n" +
                        "• يُحظر استخدام التطبيق لأغراض غير قانونية.\n\n" +

                        "4. الحسابات والتسجيل\n" +
                        "• المستخدم مسؤول عن سرية بيانات حسابه.\n" +
                        "• نحن غير مسؤولين عن أي استخدام غير مصرح به.\n" +
                        "• يحق لنا تعليق أو إلغاء الحساب في حال إساءة الاستخدام.\n\n" +

                        "5. الحجوزات والدفع\n" +
                        "• الحجوزات تخضع لشروط مزودي الخدمة.\n" +
                        "• الأسعار قابلة للتغيير حسب التوفر.\n" +
                        "• سياسات الإلغاء والاسترداد تختلف حسب نوع الحجز.\n\n" +

                        "6. إخلاء المسؤولية\n" +
                        "• التطبيق يعمل كوسيط بين المستخدم ومزودي الخدمة.\n" +
                        "• لا نتحمل مسؤولية أي تأخير أو خسائر.\n" +
                        "• المعلومات المقدمة لأغراض إرشادية فقط.\n\n" +

                        "7. الملكية الفكرية\n" +
                        "جميع المحتويات مملوكة للتطبيق أو مرخصة له، ويمنع إعادة استخدامها دون إذن خطي.\n\n" +

                        "8. الخصوصية\n" +
                        "يخضع استخدام البيانات الشخصية لسياسة الخصوصية الخاصة بالتطبيق.\n\n" +

                        "9. التعديلات\n" +
                        "نحتفظ بالحق في تعديل هذه السياسة في أي وقت، ويعد استمرار استخدام التطبيق موافقة على التعديلات.\n\n" +

                        "10. القانون المعمول به\n" +
                        "تخضع هذه السياسة لقوانين اليمن.\n\n" +

                        "التواصل معنا\n" +
                        "البريد الإلكتروني:\nMsafer@eplatx.com\n\n" +
                        "رقم الهاتف:\n967785050270\n";

        SpannableString spannable2 = new SpannableString(policyText);

        int orange = ContextCompat.getColor(this, R.color.primary); // أو أي لون تحبه

        String[] titles = {
                "سياسة الخصوصية",
                "1. التعريفات",
                "2. نطاق الخدمة",
                "3. شروط الاستخدام",
                "4. الحسابات والتسجيل",
                "5. الحجوزات والدفع",
                "6. إخلاء المسؤولية",
                "7. الملكية الفكرية",
                "8. الخصوصية",
                "9. التعديلات",
                "10. القانون المعمول به",
                "التواصل معنا"
        };

        for (String title : titles) {
            int start = policyText.indexOf(title);
            if (start >= 0) {
                int end = start + title.length();
                spannable2.setSpan(
                        new ForegroundColorSpan(orange),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }
        privacy.setText(spannable2);

        String text = "اقرأ سياسة الخصوصية الخاصة بنا. اضغط على موافقة ومتابعة لقبول شروط الخدمة.";

        SpannableString spannable = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                privacyScroll.setVisibility(VISIBLE);
                privacyScroll.setAlpha(0f);
                privacyScroll.setVisibility(View.VISIBLE);
                privacyScroll.animate().alpha(1f).setDuration(300);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(PrivacyPolicy.this, R.color.primary));
                ds.setUnderlineText(false); // بدون خط تحت النص
            }
        };

        spannable.setSpan(clickableSpan, 5, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        privacyText.setText(spannable);
        privacyText.setMovementMethod(LinkMovementMethod.getInstance());
        privacyText.setHighlightColor(Color.TRANSPARENT);
//        privacyText.setOnClickListener(v -> {
//
//        });
        agreeButton.setOnClickListener(v -> {
            agreeButton.setEnabled(false);
            agreeButton.setText("جارٍ المتابعة...");

            new Handler(Looper.getMainLooper()).post(() -> {
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().putInt("privacyAcceptedVersion", 1).apply();

                Intent intent = new Intent(PrivacyPolicy.this, HomePage.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        });

    }

}