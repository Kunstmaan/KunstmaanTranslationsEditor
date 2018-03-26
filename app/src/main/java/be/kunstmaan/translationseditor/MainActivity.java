package be.kunstmaan.translationseditor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kunstmaan.be.kunstmaantranslationseditor.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button showEditorButton = findViewById(R.id.show_button);
        showEditorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KunstmaanTranslationUtil.showTranslationsWindow();
            }
        });
    }

}
