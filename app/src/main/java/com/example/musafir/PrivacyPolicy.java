package com.example.musafir;

import static android.view.View.VISIBLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
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

        String policyText = "سياسة الخصوصية لتطبيق مسافر\n" +
                "مرحبًا بكم في تطبيق مسافر. نحن نلتزم بحماية خصوصية مستخدمينا والمحافظة على سرية بياناتهم الشخصية. باستخدامك لتطبيق مسافر أو إنشاء حساب أو الاستمرار في استخدام خدماته، فإنك توافق على هذه السياسة وعلى آلية جمع واستخدام ومعالجة البيانات الموضحة أدناه.\n" +
                "أولًا: من نحن\n" +
                "مسافر منصة رقمية متخصصة في حجز النقل البري، تتيح للمستخدمين البحث عن الرحلات، الحجز، الدفع، واستلام التذاكر عبر التطبيق، والتنسيق مع شركات النقل المعتمدة.\n" +
                "ثانيًا: البيانات التي نقوم بجمعها\n" +
                "قد نقوم بجمع البيانات التالية عند التسجيل أو استخدام التطبيق:\n" +
                "1. بيانات الحساب\n" +
                "• الاسم\n" +
                "• رقم الهاتف\n" +
                "• كلمة المرور (بصورة مشفرة)\n" +
                "2. بيانات الحجز\n" +
                "• خط السير\n" +
                "• نقطة الانطلاق والوصول\n" +
                "• تاريخ الرحلة\n" +
                "• عدد المقاعد\n" +
                "• بيانات المسافرين\n" +
                "• حالة الحجز\n" +
                "3. بيانات الهوية والوثائق\n" +
                "عند الحاجة قد نطلب:\n" +
                "• صورة الهوية\n" +
                "• صورة الجواز\n" +
                "• التأشيرة\n" +
                "• أي وثائق لازمة لإتمام الرحلة أو الامتثال لمتطلبات شركات النقل\n" +
                "4. البيانات المالية\n" +
                "• حالة الدفع\n" +
                "• رقم العملية\n" +
                "• وسيلة الدفع\n" +
                "• بيانات مرجعية لازمة للتحقق من العملية\n" +
                "5. بيانات الاستخدام الفنية\n" +
                "• نوع الجهاز\n" +
                "• نظام التشغيل\n" +
                "• عنوان IP\n" +
                "• إصدار التطبيق\n" +
                "• سجلات الأعطال والأخطاء الفنية\n" +
                "ثالثًا: كيف نستخدم البيانات\n" +
                "نستخدم البيانات للأغراض التالية:\n" +
                "• إنشاء وإدارة حساب المستخدم\n" +
                "• تنفيذ الحجوزات وإصدار التذاكر\n" +
                "• التحقق من الهوية والوثائق عند الحاجة\n" +
                "• معالجة المدفوعات ومراجعة العمليات\n" +
                "• التواصل مع المستخدم بخصوص الحجوزات\n" +
                "• إرسال الإشعارات والتنبيهات\n" +
                "• تحسين أداء التطبيق وتجربة المستخدم\n" +
                "• مكافحة الاحتيال وإساءة الاستخدام\n" +
                "• الامتثال للمتطلبات النظامية والتنظيمية عند الحاجة\n" +
                "• حفظ السجلات التشغيلية والمالية اللازمة\n" +
                "رابعًا: مشاركة البيانات\n" +
                "قد نشارك البيانات بالقدر اللازم فقط مع:\n" +
                "1. شركات النقل المتعاقد معها\n" +
                "2. مزودي خدمات الدفع\n" +
                "3. الجهات الرسمية المختصة\n" +
                "خامسًا: التحقق من الهوية والوثائق\n" +
                "يحق لتطبيق مسافر طلب التحقق من هوية المستخدم أو وثائق السفر متى كان ذلك لازمًا لإتمام الحجز، ويتحمل المستخدم مسؤولية صحة الوثائق والبيانات المقدمة.\n" +
                "سادسًا: مسؤولية صحة البيانات\n" +
                "يلتزم المستخدم بإدخال بيانات صحيحة وكاملة ومطابقة للوثائق الرسمية، ويتحمل مسؤولية أي تأخير أو رفض حجز أو خسارة تنتج عن إدخال بيانات غير صحيحة.\n" +
                "سابعًا: حماية البيانات\n" +
                "نحرص على اتخاذ إجراءات تقنية وإدارية مناسبة لحماية البيانات من الوصول غير المصرح به أو الاستخدام غير المشروع.\n" +
                "ثامنًا: الاحتفاظ بالبيانات والسجلات\n" +
                "نحتفظ بالبيانات والسجلات الإلكترونية للمدة اللازمة لتحقيق الأغراض التشغيلية أو القانونية أو التنظيمية.\n" +
                "تاسعًا: حقوق المستخدم\n" +
                "يجوز للمستخدم طلب الاطلاع على بياناته، تحديث البيانات غير الصحيحة، حذف الحساب، أو الاستفسار عن كيفية استخدام بياناته.\n" +
                "عاشرًا: ملفات الارتباط والتقنيات المشابهة\n" +
                "قد نستخدم ملفات تعريف الارتباط أو أدوات تحليلية داخل التطبيق لتحسين الأداء وقياس الاستخدام وتطوير الخدمات.\n" +
                "الحادي عشر: الإشعارات والتواصل\n" +
                "قد نرسل إشعارات متعلقة بحالة الحجز، التذكرة، المدفوعات، التحديثات المهمة، أو العروض.\n" +
                "الثاني عشر: حدود مسؤولية المنصة\n" +
                "يعمل تطبيق مسافر كمنصة حجز وربط وتنظيم، بينما تقع مسؤولية تنفيذ الرحلة ومواعيدها والتشغيل الميداني على شركة النقل المعنية.\n" +
                "الثالث عشر: الامتثال المالي\n" +
                "تخضع عمليات الدفع والاسترجاع للإجراءات والأنظمة المعمول بها لدى مزودي خدمات الدفع والمؤسسات المالية المعتمدة.\n" +
                "الرابع عشر: خصوصية الأطفال\n" +
                "الخدمة موجهة للمستخدمين القادرين نظاميًا على التعاقد، ولا تستهدف جمع بيانات الأطفال عمدًا.\n" +
                "الخامس عشر: التعديلات على السياسة\n" +
                "قد نقوم بتحديث هذه السياسة من وقت لآخر، ويعد استمرار استخدام الخدمة بعد التحديث موافقة على النسخة المعدلة.\n" +
                "# السادس عشر: القانون الواجب التطبيق والاختصاص\n" +
                "تخضع هذه السياسة وأي نزاع ينشأ عنها للقوانين النافذة في الجمهورية اليمنية.\n" +
                "السابع عشر: اللغة المعتمدة\n" +
                "تكون النسخة العربية هي المرجع المعتمد عند التفسير ما لم ينص على خلاف ذلك.\n" +
                "الثامن عشر: الموافقة على السياسة\n" +
                "باستخدامك لتطبيق مسافر أو إنشاء حساب، فإنك تقر بأنك قرأت هذه السياسة وفهمتها وتوافق عليها.\n" +
                "التاسع عشر: التواصل معنا\n" +
                "لأي استفسار يتعلق بالخصوصية أو البيانات:\n" +
                "اسم الجهة: مسافر\n" +
                "الهاتف: 967785050270\n" +
                "واتساب: 967785050270";

//        SpannableString spannable2 = new SpannableString(policyText);
//
//        int orange = ContextCompat.getColor(this, R.color.primary);

//        String[] titles = {
//                "أولًا:", "ثانيًا:", "ثالثًا:", "رابعًا:", "خامسًا:", "سادسًا:", "سابعًا:", "ثامنًا:", "تاسعًا:", "عاشرًا:",
//                "الحادي عشر:", "الثاني عشر:", "الثالث عشر:", "الرابع عشر:", "الخامس عشر:", "السادس عشر:", "السابع عشر:", "الثامن عشر:", "التاسع عشر:"
//        };
//
//        for (String title : titles) {
//            int start = policyText.indexOf(title);
//            if (start >= 0) {
//                int end = start + title.length();
//                spannable2.setSpan(
//                        new ForegroundColorSpan(orange),
//                        start,
//                        end,
//                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                );
//            }
//        }
        privacy.setText(policyText);

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
        agreeButton.setOnClickListener(v -> {
            agreeButton.setEnabled(false);
            agreeButton.setText("جارٍ المتابعة...");

            new Handler(Looper.getMainLooper()).post(() -> {
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().putInt("privacyAcceptedVersion", 1).apply();

                Intent intent;
                String inviteCode = getIntent().getStringExtra("invite");

                if (inviteCode != null && !inviteCode.isEmpty()) {
                    intent = new Intent(PrivacyPolicy.this, MainActivity2.class);
                    intent.putExtra("invite", inviteCode);
                } else {
                    intent = new Intent(PrivacyPolicy.this, HomePage.class);
                }

                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            });
        });

    }

}
