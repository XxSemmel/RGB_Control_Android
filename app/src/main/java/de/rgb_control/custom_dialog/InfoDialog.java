package de.rgb_control;



import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class InfoDialog extends Dialog implements View.OnClickListener {

    private String text;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.infodialog);

        Button btn = findViewById(R.id.buttonOk);
        TextView textv = findViewById(R.id.info_text);
        TextView tilev = findViewById(R.id.info_title);
        textv.setText(text);
        tilev.setText(title);
        btn.setOnClickListener(this);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        this.getWindow().setAttributes(lp);
        //lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        //.getWindow().setAttributes(lp);





    }

   public InfoDialog(Activity a, String text, String title){
       super(a);
       this.text=text;
       this.title=title;





   }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonOk:
                dismiss();
        }
    }
}
